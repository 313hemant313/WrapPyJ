# Contributing to WrapPyJ

Thank you for your interest in contributing to WrapPyJ! This document provides guidelines and information for contributors to help make the contribution process smooth and effective.

## 🤝 How to Contribute

We welcome contributions from the community! There are many ways to contribute:

- 🐛 **Bug Reports**: Report issues you encounter
- 💡 **Feature Requests**: Suggest new features or improvements
- 📝 **Documentation**: Improve or add documentation
- 🔧 **Code Contributions**: Submit pull requests with code changes
- 🧪 **Testing**: Help test the project and report issues
- 💬 **Discussions**: Participate in discussions and provide feedback

## 📋 Table of Contents

- [Code of Conduct](#code-of-conduct)
- [Development Setup](#development-setup)
- [Project Structure](#project-structure)
- [Coding Standards](#coding-standards)
- [Testing Guidelines](#testing-guidelines)
- [Pull Request Process](#pull-request-process)
- [Issue Reporting](#issue-reporting)
- [Release Process](#release-process)
- [Community Guidelines](#community-guidelines)

## 📜 Code of Conduct

### Our Standards

We are committed to providing a welcoming and inspiring community for all. By participating in this project, you agree to:

- **Be respectful and inclusive** - Treat everyone with respect
- **Be collaborative** - Work together for the benefit of the project
- **Be constructive** - Provide constructive feedback and suggestions
- **Be professional** - Maintain professional behavior in all interactions

### Unacceptable Behavior

The following behaviors are considered unacceptable:

- Harassment, discrimination, or offensive comments
- Trolling, insulting, or derogatory comments
- Publishing others' private information without permission
- Other conduct that could reasonably be considered inappropriate

### Enforcement

Violations of the Code of Conduct may result in:
- Temporary or permanent ban from the project
- Removal of contributions
- Other appropriate actions as determined by project maintainers

## 🛠️ Development Setup

### Prerequisites

- **Java 19+**: Required for development
- **Maven 3.6+**: Build tool
- **Python 3.10+**: For testing Python integration
- **Git**: Version control
- **IDE**: IntelliJ IDEA, Eclipse, or VS Code recommended

### Local Development Environment

1. **Fork and Clone**
   ```bash
   git clone https://github.com/your-username/WrapPyJ.git
   cd WrapPyJ
   ```

2. **Set up Git hooks** (optional but recommended)
   ```bash
   cp .git-hooks/* .git/hooks/
   chmod +x .git/hooks/*
   ```

3. **Build the project**
   ```bash
   mvn clean install
   ```

4. **Run tests**
   ```bash
   mvn test
   ```

5. **Run example applications**
   ```bash
   # Generate wrappers
   cd example-generator
   mvn compile
   
   # Run usage examples
   cd ../example-wrapper-usage
   mvn spring-boot:run
   ```

### IDE Configuration

#### IntelliJ IDEA
1. Import as Maven project
2. Enable annotation processing for Lombok
3. Set Java version to 19
4. Configure Python interpreter for testing

#### Eclipse
1. Import as Maven project
2. Install Lombok plugin
3. Configure Java 19
4. Set up Python environment

#### VS Code
1. Install Java Extension Pack
2. Install Python extension
3. Configure Java 19
4. Set up Maven integration

## 🏗️ Project Structure

Understanding the project structure is crucial for effective contributions:

```
WrapPyJ/
├── generator/                    # Core generation engine
│   ├── src/main/java/
│   │   └── tech/thegamedefault/wrappyj/generator/
│   │       ├── framework/        # Core framework classes
│   │       ├── model/           # Data models
│   │       ├── service/         # Business logic services
│   │       └── utility/         # Utility classes
│   └── src/main/resources/      # Configuration and resources
├── example-generator/           # Example wrapper generation
├── example-wrapper-usage/       # Example wrapper usage
└── src/main/java/.../generated/ # Generated wrapper classes
```

### Key Components

- **`PythonAnalyser`**: Analyzes Python library structure
- **`JavaWrapperGenerator`**: Generates Java wrapper classes
- **`JPInterpreter`**: Manages Python interpreter instances
- **`PythonRuntimeManager`**: Handles Python runtime setup

## 📝 Coding Standards

### Java Code Style

We follow the [Google Java Style Guide](https://google.github.io/styleguide/intellij-java-google-style.xml) for code formatting and conventions. This ensures consistency across the codebase and aligns with industry best practices.

#### IDE Configuration
To automatically apply the Google Java Style Guide:

1. **IntelliJ IDEA**:
   - Go to `File` → `Settings` → `Editor` → `Code Style`
   - Click the gear icon → `Import Scheme` → `IntelliJ IDEA code style XML`
   - Import the [Google Java Style Guide XML](https://google.github.io/styleguide/intellij-java-google-style.xml)
   - Apply the scheme to your project

2. **Eclipse**:
   - Install the Google Style plugin
   - Import the Google Java Style Guide settings
   - Apply to your workspace

3. **VS Code**:
   - Install the Java Extension Pack
   - Configure the formatter to use Google Style

#### Key Style Guidelines
- **Indentation**: 2 spaces
- **Line Length**: Maximum 100 characters (Google standard)
- **Braces**: K&R style (opening brace on same line)
- **Imports**: Organized and unused imports removed
- **Naming Conventions**:
  - **Classes**: PascalCase (e.g., `PythonAnalyser`)
  - **Methods**: camelCase (e.g., `generateWrapper`)
  - **Constants**: UPPER_SNAKE_CASE (e.g., `DEFAULT_PACKAGE`)
  - **Packages**: lowercase (e.g., `tech.thegamedefault.wrappyj`)

#### Example
```java
public class ExampleClass {
  private static final String DEFAULT_VALUE = "example";
  
  public void exampleMethod(String parameter) {
    if (parameter != null) {
      // Implementation
    }
  }
}
```

### Documentation Standards

#### JavaDoc Requirements
- All public classes and methods must have JavaDoc
- Include `@param`, `@return`, and `@throws` where applicable
- Use clear, concise descriptions

```java
/**
 * Generates Java wrapper classes for Python libraries.
 * 
 * @param requests List of generation requests
 * @throws RuntimeException if generation fails
 */
public static void generate(List<GeneratorRequest> requests) {
  // Implementation
}
```

#### README Updates
- Update relevant README sections when adding features
- Include usage examples for new functionality
- Update configuration examples if needed

### Error Handling

- Use appropriate exception types
- Provide meaningful error messages
- Log errors with appropriate levels
- Include context information in exceptions

```java
try {
  // Operation
} catch (SpecificException e) {
  log.error("Failed to perform operation: {}", e.getMessage(), e);
  throw new WrapperGenerationException("Operation failed", e);
}
```

## 🧪 Testing Guidelines

### Test Structure

Tests should be organized to mirror the main source structure:

```
src/test/java/
└── tech/thegamedefault/wrappyj/generator/
    ├── framework/
    ├── model/
    ├── service/
    └── utility/
```

### Test Naming

- **Test Classes**: `{ClassName}Test`
- **Test Methods**: `test{MethodName}_{Scenario}`

```java
public class PythonAnalyserTest {
  @Test
  public void testAnalyse_ValidLibrary_ReturnsMetadata() {
    // Test implementation
  }
  
  @Test
  public void testAnalyse_InvalidLibrary_ThrowsException() {
    // Test implementation
  }
}
```

### Test Categories

#### Unit Tests
- Test individual methods in isolation
- Use mocks for dependencies
- Focus on specific functionality

#### Integration Tests
- Test component interactions
- Use real dependencies where appropriate
- Test end-to-end workflows

#### Python Integration Tests
- Test actual Python library integration
- Use small, focused Python libraries for testing
- Ensure Python environment is properly configured

### Test Coverage

- Aim for at least 80% code coverage
- Focus on critical paths and edge cases
- Include positive and negative test cases
- Test error conditions and exception handling

### Running Tests

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=PythonAnalyserTest

# Run with coverage
mvn test jacoco:report

# Run integration tests only
mvn test -Dtest=*IntegrationTest
```

## 🔄 Pull Request Process

### Before Submitting

1. **Check existing issues** - Ensure your change isn't already being worked on
2. **Create an issue** - For significant changes, create an issue first
3. **Update documentation** - Ensure README and JavaDoc are updated
4. **Add tests** - Include tests for new functionality
5. **Run full test suite** - Ensure all tests pass

### Pull Request Guidelines

#### Title and Description
- **Title**: Clear, concise description of changes
- **Description**: Detailed explanation of what and why
- **Fixes**: Reference related issues (e.g., "Fixes #123")

#### Example PR Description
```markdown
## Description
Adds support for custom Python package installation during wrapper generation.

## Changes
- Added `PythonPackageInstaller` class
- Updated `PythonRuntimeManager` to use custom installer
- Added configuration options for package sources

## Testing
- Added unit tests for `PythonPackageInstaller`
- Added integration tests for custom package installation
- Updated existing tests to work with new functionality

## Documentation
- Updated README with new configuration options
- Added JavaDoc for new classes and methods

Fixes #456
```

#### Code Review Checklist

Before submitting, ensure your PR:

- [ ] Follows coding standards
- [ ] Includes appropriate tests
- [ ] Updates documentation
- [ ] Handles errors appropriately
- [ ] Includes meaningful commit messages
- [ ] Passes all CI checks

### Commit Message Guidelines

Use conventional commit format:

```
type(scope): description

[optional body]

[optional footer]
```

#### Types
- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation changes
- `style`: Code style changes
- `refactor`: Code refactoring
- `test`: Test changes
- `chore`: Build/tool changes

#### Examples
```
feat(generator): add support for custom Python packages

fix(analyser): handle missing Python module gracefully

docs(readme): update installation instructions

test(wrapper): add integration tests for pandas
```

## 🐛 Issue Reporting

### Bug Reports

When reporting bugs, please include:

1. **Clear description** of the problem
2. **Steps to reproduce** the issue
3. **Expected behavior** vs actual behavior
4. **Environment details**:
   - Java version
   - Python version
   - Operating system
   - Maven version
5. **Error messages** and stack traces
6. **Minimal example** that reproduces the issue

#### Bug Report Template
```markdown
## Bug Description
[Clear description of the bug]

## Steps to Reproduce
1. [Step 1]
2. [Step 2]
3. [Step 3]

## Expected Behavior
[What should happen]

## Actual Behavior
[What actually happens]

## Environment
- Java: [version]
- Python: [version]
- OS: [operating system]
- Maven: [version]

## Error Messages
[Any error messages or stack traces]

## Additional Information
[Any other relevant information]
```

### Feature Requests

When requesting features, please include:

1. **Clear description** of the feature
2. **Use case** and motivation
3. **Proposed implementation** (if you have ideas)
4. **Alternatives considered** (if any)

## 🚀 Release Process

### Versioning

We follow [Semantic Versioning](https://semver.org/):

- **Major**: Breaking changes
- **Minor**: New features (backward compatible)
- **Patch**: Bug fixes (backward compatible)

### Release Checklist

Before each release:

- [ ] All tests pass
- [ ] Documentation is updated
- [ ] Changelog is updated
- [ ] Version numbers are updated
- [ ] Release notes are prepared
- [ ] Tag is created
- [ ] Release is published

### Release Steps

1. **Update version** in `pom.xml` files
2. **Update changelog** with new features/fixes
3. **Create release branch** from main
4. **Run full test suite** on release branch
5. **Create release tag**
6. **Merge to main** and push
7. **Create GitHub release** with notes
8. **Update documentation** if needed

## 👥 Community Guidelines

### Communication

- **Be respectful** in all interactions
- **Ask questions** when you need clarification
- **Provide context** when reporting issues
- **Help others** when you can

### Getting Help

- **Check documentation** first
- **Search existing issues** for similar problems
- **Ask in discussions** for general questions
- **Create issues** for bugs and feature requests

### Recognition

Contributors will be recognized in:
- **README** contributors section
- **Release notes** for significant contributions
- **GitHub** contributor statistics

## 📚 Additional Resources

### Documentation
- [Project README](./README.md)
- [API Documentation](./docs/api.md)
- [Architecture Guide](./docs/architecture.md)

### Tools and Setup
- [Maven Documentation](https://maven.apache.org/guides/)
- [JEP Documentation](https://github.com/ninia/jep)
- [JavaPoet Documentation](https://github.com/square/javapoet)
- [Google Java Style Guide](https://google.github.io/styleguide/intellij-java-google-style.xml) - Code formatting standards

### Community
- [GitHub Discussions](https://github.com/your-org/WrapPyJ/discussions)
- [Issue Tracker](https://github.com/your-org/WrapPyJ/issues)
- [Pull Requests](https://github.com/your-org/WrapPyJ/pulls)

---

Thank you for contributing to WrapPyJ! Your contributions help make this project better for everyone. 🎉

If you have any questions about contributing, please don't hesitate to ask in the discussions or create an issue. 