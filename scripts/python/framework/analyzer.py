import importlib
import inspect, re
import pkgutil
import sys
import io
from typing import Dict, Any, List

SKIP_PATTERNS = ('.f2py',)
VERBOSE       = False

if VERBOSE and hasattr(sys.stdout, "buffer"):
  sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding="utf-8")
  sys.stderr = io.TextIOWrapper(sys.stderr.buffer, encoding="utf-8")

def safe_import(name: str):
  """
  Import a module, but intercept `sys.exit()` (raised as SystemExit) so that
  CLI entry-points like numpy.f2py cannot terminate the host interpreter.
  """
  try:
    return importlib.import_module(name)
  except SystemExit as e:
    if VERBOSE:
      print(f"[skip] {name} tried to exit (code={e.code}). Skipping.")
    return None
  except Exception as err:
    if VERBOSE:
      print(f"[skip] Could not import {name}: {err}")
    return None


# ──────────────────────────────────────────────────────────────────────────────
#  Helpers for signature / docstring extraction
# ──────────────────────────────────────────────────────────────────────────────

def _is_callable(obj):
  """True for ANY python callable (functions, ufuncs, Cython objects …)."""
  return callable(obj)

def parse_text_signature(sig: str) -> List[str]:
  if not sig:
    return []
  sig = sig.strip().strip("()")
  args = []
  for arg in sig.split(","):
    arg = arg.strip()
    if arg in {"/", "*"} or not arg:
      continue
    name = arg.split("=")[0].strip()
    args.append(name)
  return args


def _count_optional(p: inspect.Parameter) -> bool:
  return (
      p.default is not inspect.Parameter.empty
      or p.kind
      in {inspect.Parameter.VAR_POSITIONAL, inspect.Parameter.VAR_KEYWORD}
  )


def _parse_sig(obj):
  try:
    sig = inspect.signature(obj)
    args = [p.name for p in sig.parameters.values()]
    opt = sum(_count_optional(p) for p in sig.parameters.values())
    return args, opt
  except (TypeError, ValueError):
    return [], 0


def get_function_metadata(func, module_name: str) -> Dict[str, Any]:
  try:
    args, opt = _parse_sig(func)
    doc = inspect.getdoc(func) or ""
  except Exception:
    args, opt, doc = [], 0, ""

  if not args:
    text_sig = getattr(func, "__text_signature__", None)
    if text_sig:
      args = parse_text_signature(text_sig)
      opt  = sum("=" in p for p in text_sig.strip("()").split(","))
    elif doc:
      m = re.match(r"^\s*[a-zA-Z_][a-zA-Z0-9_]*\((.*?)\)", doc)
      if m:
        sig_part = m.group(1)
        pieces   = [p.strip() for p in sig_part.split(",") if p.strip() not in {"/", "*"}]
        args     = [p.split("=")[0].strip() for p in pieces]
        opt      = sum("=" in p for p in pieces)

  return {
    "name": getattr(func, "__name__", repr(func)),
    "args": args,
    "optionalCount": opt,
    "doc": doc,
    "module": module_name,
  }


def get_class_metadata(cls) -> Dict[str, Any]:
  methods = []
  for name, member in inspect.getmembers(cls, predicate=inspect.isfunction):
    if name.startswith("__") and name.endswith("__") and name != "__init__":
      continue
    args, opt = _parse_sig(member)
    methods.append(
      {
        "name": name,
        "args": args,
        "optionalCount": opt,
        "doc": inspect.getdoc(member) or "",
      }
    )
  return {
    "name": cls.__name__,
    "doc": inspect.getdoc(cls) or "",
    "methods": methods,
    "module": cls.__module__,
  }

# ──────────────────────────────────────────────────────────────────────────────
#  Main entry
# ──────────────────────────────────────────────────────────────────────────────
def analyze_library(lib_name: str, include_only: list[str] | None = None) -> Dict[str, Any]:
  """
  Recursively analyses *lib_name* and returns metadata for its callables.
  Any sub-module that calls `sys.exit()` (or matches SKIP_PATTERNS) is skipped.
  """
  root = safe_import(lib_name)
  if root is None:
    return {"error": f"Failed to import {lib_name}"}

  allow = set(include_only or ())
  functions: Dict[str, Any] = {}
  classes: Dict[str, Any] = {}

  def _want(name: str) -> bool:
    """Return True if this symbol should be kept."""
    return not allow or name in allow


  def visit(mod):
    if mod is None:
      return
    # ----- functions -----
    for _, obj in inspect.getmembers(mod, _is_callable):
      module_name = (getattr(obj, "__module__", "") or "")
      if not module_name.startswith(lib_name):
        continue
      fn_name = getattr(obj, "__name__", repr(obj))
      if not _want(fn_name):
        continue
      if fn_name not in functions:
        functions[fn_name] = get_function_metadata(obj, module_name)
    # ----- classes -----
    for _, cls in inspect.getmembers(mod, inspect.isclass):
      module_name = (getattr(cls, "__module__", "") or "")
      if not module_name.startswith(lib_name):
        continue
      if not _want(cls.__name__):
        continue
      key = f"{module_name}.{cls.__name__}"
      if key not in classes:
        classes[key] = get_class_metadata(cls)

  # root module first
  visit(root)

  # recurse into sub-packages
  if not allow and hasattr(root, "__path__"):
    def _skipper(name: str):
      # pkgutil hit an ImportError / OSError while looking at `name`
      if VERBOSE:
        print(f"[skip] {name} raised at scan-time")

    for _, subname, _ in pkgutil.walk_packages(
        root.__path__,
        root.__name__ + ".",
        onerror=_skipper):
      if subname.endswith(SKIP_PATTERNS):
        if VERBOSE:
          print(f"[skip] {subname} matches SKIP_PATTERNS")
        continue
      visit(safe_import(subname))

  return {
    "library": lib_name,
    "functions": list(functions.values()),
    "classes": list(classes.values()),
  }


# ──────────────────────────────────────────────────────────────────────────────
#  Test
# ──────────────────────────────────────────────────────────────────────────────
if __name__ == "__main__":
  print("Scanning numpy …")
  meta = analyze_library("numpy")
  print(f"found {len(meta['functions'])} functions and {len(meta['classes'])} classes")
