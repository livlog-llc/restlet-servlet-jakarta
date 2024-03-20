# Restlet Servlet Jakarta Extension

This project provides an extension for the Restlet Framework to support the Jakarta Servlet API, enabling developers to build modern web applications with the latest standards.

[![](https://jitpack.io/v/livlog-llc/restlet-servlet-jakarta.svg)](https://jitpack.io/#livlog-llc/restlet-servlet-jakarta)

## Overview

The Restlet Framework is a powerful, lightweight framework for building RESTful web services in Java. However, with the transition from `javax.servlet` to `jakarta.servlet`, there was a need to update the Restlet's Servlet extension to be compatible with the latest Jakarta Servlet API. This project, `restlet-servlet-jakarta`, fills that gap, allowing developers to use Restlet with Jakarta Servlet API seamlessly.

## Features

- Full compatibility with Jakarta Servlet API 6.0.0.
- Integration with Restlet Framework version 2.4.3.
- Support for Java 17.

## Getting Started

To use this extension in your Maven project, add the following to your `pom.xml`:

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>com.github.livlog-llc</groupId>
        <artifactId>restlet-servlet-jakarta</artifactId>
        <version>2.4.3</version>
    </dependency>
</dependencies>
```

## Building from Source

To build the project from source, you will need:

- JDK 17 or later
- Maven 3.6 or later

Clone the repository and run the following command in the project root:

```bash
mvn clean install
```

## Contributing

We welcome contributions from the community! If you would like to contribute to the project, please follow these steps:

1. Fork the repository.
2. Create a new branch for your feature or bug fix.
3. Commit your changes with meaningful commit messages.
4. Push your branch and submit a pull request.

Please make sure your code adheres to the project's coding standards and passes all tests.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Acknowledgments

- Thanks to the Restlet Framework team for providing a solid foundation for building RESTful web services.
- Thanks to all contributors who have helped to make this project better.
