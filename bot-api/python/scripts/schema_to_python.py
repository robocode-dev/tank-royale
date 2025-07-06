import argparse
import re
import yaml
from pathlib import Path
from typing import Dict, Any, List, Optional, Set, Tuple


class SchemaConverter:
    """Converts JSON schema definitions to Python classes."""

    def __init__(self) -> None:
        self.processed_schemas: Dict[str, Any] = {}
        self.class_dependencies: Dict[str, Set[str]] = {}
        self.output_dir: Optional[Path] = None
        self.schema_properties_cache: Dict[
            str, Tuple[Set[str], Set[str], Dict[str, Any]]
        ] = {}

    def get_python_type(self, prop_schema: Dict[str, Any], file_path: str) -> str:
        """Determine the Python type for a schema property."""
        if "$ref" in prop_schema:
            class_name, ref_file = SchemaUtils.resolve_reference(prop_schema["$ref"])
            if ref_file:
                self.class_dependencies.setdefault(file_path, set()).add(ref_file)
            return f"{class_name} | None"

        property_type = prop_schema.get("type")
        assert property_type is not None

        simple_type_mapping = {
            "string": "str | None",
            "integer": "int | None",
            "number": "float | None",
            "boolean": "bool | None",
            "object": "dict | None",
        }
        simple_mapped_type = simple_type_mapping.get(property_type, None)
        if simple_mapped_type is not None:
            return simple_mapped_type
        # property_type is not simple
        if property_type == "array":
            item_schema = prop_schema.get("items")
            assert item_schema is not None
            item_mapped_type: str = self.get_python_type(item_schema, file_path)
            return f"list[{item_mapped_type}] | None"

        raise NotImplementedError(f"Cannot handle property type {property_type}")

    def get_parent_schema_info(
        self, schema: Dict[str, Any], current_file_path: str
    ) -> Tuple[Set[str], Set[str], Dict[str, Any]]:
        """Get the properties and requirements defined in the parent class."""
        if "extends" not in schema or "$ref" not in schema["extends"]:
            return set(), set(), {}

        base_class_ref = schema["extends"]["$ref"]
        base_file = SchemaUtils.resolve_reference(base_class_ref)[1]
        cache_key = f"{current_file_path}:{base_file}"

        if cache_key in self.schema_properties_cache:
            return self.schema_properties_cache[cache_key]

        # Use the directory of the current file to resolve the parent file path
        current_dir = Path(current_file_path).parent
        parent_file_path = str(current_dir / base_file)

        try:
            parent_schema = SchemaUtils.load_yaml(parent_file_path)
            parent_properties = SchemaUtils.convert_dict_keys_to_snake(
                parent_schema.get("properties", {})
            )
            parent_required = set(
                SchemaUtils.convert_list_to_snake(parent_schema.get("required", []))
            )

            # If the parent also has a parent, include those properties too
            if "extends" in parent_schema and "$ref" in parent_schema["extends"]:
                _, grandparent_required, grandparent_schema = (
                    self.get_parent_schema_info(parent_schema, parent_file_path)
                )
                parent_properties.update(grandparent_schema)
                parent_required.update(grandparent_required)

            result = (set(parent_properties.keys()), parent_required, parent_properties)
            self.schema_properties_cache[cache_key] = result
            return result

        except FileNotFoundError:
            print(f"Warning: Could not find parent schema file: {parent_file_path}")
            return set(), set(), {}

    def generate_class(
        self, class_name: str, schema: Dict[str, Any], file_path: str
    ) -> str:
        """Generate a Python class definition from a JSON schema."""
        description = schema.get("description", "")

        # Handle simple type schemas
        if (
            not "extends" in schema
            and "type" in schema
            and isinstance(schema["type"], str)
            and "properties" not in schema
        ):
            python_type = self.get_python_type({"type": schema["type"]}, file_path)
            class_lines = [f"class {class_name}:"]

            if description:
                class_lines.extend([f'    """{description}"""', ""])

            class_lines.extend(
                [
                    f"    def __init__(self, value: {python_type}):",
                    "        if value is None:",
                    "            raise ValueError(\"The 'value' parameter must be provided.\")",
                    "        self.value = value",
                    "",
                ]
            )
            return "\n".join(class_lines)

        # Process complex object schemas
        properties = SchemaUtils.convert_dict_keys_to_snake(
            schema.get("properties", {})
        )
        required = SchemaUtils.convert_list_to_snake(schema.get("required", []))

        # Handle inheritance
        base_classes: list[str] = []
        if "extends" in schema and "$ref" in schema["extends"]:
            base_class, base_file = SchemaUtils.resolve_reference(
                schema["extends"]["$ref"]
            )
            if base_class:
                base_classes.append(base_class)
                if base_file:
                    self.class_dependencies.setdefault(file_path, set()).add(base_file)

        inheritance = f"({', '.join(base_classes)})" if base_classes else ""
        class_lines = [f"class {class_name}{inheritance}:"]

        if description:
            class_lines.extend([f'    """{description}"""', ""])

        # Process enum fields and generate nested enum classes
        enum_definitions: list[str] = []
        for prop_name, prop_schema in properties.items():
            if "enum" in prop_schema:
                enum_class_name = f"{prop_name.capitalize()}"
                enum_items = prop_schema["enum"]
                enum_definition = [f"    class {enum_class_name}(str, Enum):"]
                for item in enum_items:
                    # Convert to UPPER_SNAKE_CASE for Python enum convention
                    enum_member_name = SchemaUtils.camel_to_upper_snake(item)
                    enum_definition.append(f'        {enum_member_name} = "{item}"')
                enum_definition.append("")  # Add blank line after enum
                enum_definitions.append("\n".join(enum_definition))

        # Add enum definitions to class
        if enum_definitions:
            class_lines.append("")  # Add blank line before enums
            class_lines.extend(enum_definitions)

        # Get parent properties and requirements
        parent_properties, parent_required, parent_schema = self.get_parent_schema_info(
            schema, file_path
        )

        # Separate required and optional parameters
        required_params: list[str] = []
        optional_params: list[str] = []

        # Add all parameters, including those from parent
        all_properties = properties.copy()
        if parent_schema:
            all_properties.update(parent_schema)

        for prop_name, prop_schema in all_properties.items():
            python_name = prop_name
            if "enum" in prop_schema:
                # Use the enum class name as the type
                prop_type = f"'Message.{python_name.capitalize()} | None'"
            else:
                prop_type = self.get_python_type(prop_schema, file_path)

            if prop_name in required or prop_name in parent_required:
                required_params.append(f"{python_name}: {prop_type}")
            else:
                optional_params.append(f"{python_name}: {prop_type} = None")

        # Combine parameters with required first, then optional
        init_params = required_params + optional_params
        params_str = ", ".join(init_params)

        init_body = [
            f'    def __init__(self{", " + params_str if params_str else ""}):'
        ]

        # Add validation for required parameters
        all_required = set(required).union(parent_required)
        for prop_name in all_required:
            init_body.append(f"        if {prop_name} is None:")
            init_body.append(
                f"            raise ValueError(\"The '{prop_name}' parameter must be provided.\")"
            )

        # Pass parent properties to super().__init__
        if base_classes:
            super_params: list[str] = []
            for param in init_params:
                param_name = param.split(":")[0]
                if param_name in parent_properties:
                    super_params.append(param_name)

            if super_params:
                super_call = f"        super().__init__({', '.join(super_params)})"
                init_body.append(super_call)

        # Only set properties defined in this class (not parent)
        for prop_name in properties:
            init_body.append(f"        self.{prop_name} = {prop_name}")

        class_lines.extend(init_body)
        class_lines.append("")
        return "\n".join(class_lines)

    def process_schema_file(self, file_path: str) -> str:
        """
        Process a single schema file and return the generated class code,
        whether it has enums, and whether it uses Type.
        """
        schema_data = SchemaUtils.load_yaml(file_path)
        class_name = SchemaUtils.sanitize_class_name(Path(file_path).name)
        return self.generate_class(class_name, schema_data, file_path)

    def generate_import_statements(self, file_path: str, class_code: str) -> List[str]:
        """Generate import statements for a given schema file."""
        imports: list[str] = []

        # Add Enum import if the class contains enum definitions
        if "class " in class_code and "(str, Enum)" in class_code:
            imports.append("from enum import Enum")

        if "Message" in class_code and 'message.schema.yaml' not in file_path:
            imports.append("from .message import Message")

        if file_path in self.class_dependencies:
            for dep_file in self.class_dependencies[file_path]:
                module_name = SchemaUtils.sanitize_file_name(dep_file)
                class_name = SchemaUtils.sanitize_class_name(dep_file)
                new_import = f"from .{module_name} import {class_name}"
                if new_import not in imports:
                    imports.append(new_import)
        
        return imports

    def process_directory(self, directory_path: str, output_dir: str) -> None:
        """Process all schema files in a directory and generate Python classes."""
        self.output_dir = Path(output_dir)
        self.output_dir.mkdir(parents=True, exist_ok=True)

        yaml_files = list(Path(directory_path).glob("**/*.yaml"))
        yaml_files.extend(Path(directory_path).glob("**/*.yml"))

        sub_modules: list[str] = []
        for file_path in yaml_files:
            class_code = self.process_schema_file(str(file_path))

            if class_code:
                module_name = f"{SchemaUtils.sanitize_file_name(str(file_path))}"
                sub_modules.append(module_name)

                output_file = self.output_dir / f"{module_name}.py"

                content = [
                    '"""',
                    f"Generated Python class from {file_path.name}",
                    "This file is auto-generated. Do not edit manually.",
                    '"""',
                    "",
                ]

                content.extend(
                    self.generate_import_statements(str(file_path), class_code)
                )
                content.append("")
                content.append(class_code)

                with open(output_file, "w") as f:
                    f.write("\n".join(content))

        SchemaUtils.generate_dunder_init_py(
            output_dir=output_dir, sub_modules=sub_modules
        )


class SchemaUtils:
    """Utilities for schema conversion and naming conventions."""

    @staticmethod
    def camel_to_snake(name: str) -> str:
        """Convert a camelCase string to snake_case."""
        return re.sub(r"(?<!^)(?=[A-Z])", "_", name).lower()

    @staticmethod
    def camel_to_upper_snake(name: str) -> str:
        """Convert a camelCase or PascalCase string to UPPER_SNAKE_CASE."""
        return re.sub(r"(?<!^)(?=[A-Z])", "_", name).upper()

    @staticmethod
    def convert_dict_keys_to_snake(properties: Dict[str, Any]) -> Dict[str, Any]:
        """Convert all dictionary keys from camelCase to snake_case."""
        return {
            SchemaUtils.camel_to_snake(key): value for key, value in properties.items()
        }

    @staticmethod
    def convert_list_to_snake(items: List[str]) -> List[str]:
        """Convert a list of camelCase strings to snake_case."""
        return [SchemaUtils.camel_to_snake(item) for item in items]

    @staticmethod
    def load_yaml(file_path: str) -> Dict[str, Any]:
        """Load and parse a YAML file."""
        with open(file_path, "r") as f:
            return yaml.safe_load(f)

    @staticmethod
    def sanitize_file_name(name: str) -> str:
        """Convert a schema filename to a valid Python module name."""
        name = str(Path(name).stem).lower()
        name = name.replace(".schema", "")
        return name.replace("-", "_")

    @staticmethod
    def sanitize_class_name(name: str) -> str:
        """Convert a schema filename to a valid Python class name."""
        name = Path(name).stem
        name = name.replace(".schema", "")
        words = name.replace("-", "_").split("_")
        return "".join(word.title() for word in words)

    @staticmethod
    def resolve_reference(ref: str) -> Tuple[str, str]:
        """Resolve a schema reference to a class name and file path."""
        if not ref:
            return "", ""
        ref_path = Path(ref)
        return SchemaUtils.sanitize_class_name(ref_path.stem), ref_path.name

    @staticmethod
    def generate_dunder_init_py(output_dir: str, sub_modules: List[str]) -> None:
        """
        Generate an __init__.py file with proper imports and __all__ declarations.

        Args:
            output_dir: Directory where the __init__.py will be created
            sub_modules: List of module names (without .py extension)
        """
        output_path = Path(output_dir)
        init_file_path = output_path / "__init__.py"

        # Generate import statements and collect class names
        import_statements: list[str] = []
        class_names: list[str] = []

        for module in sub_modules:
            class_name = "".join(part.capitalize() for part in module.split("_"))
            import_statements.append(f"from .{module} import {class_name}")
            class_names.append(class_name)

        # Create file content with proper formatting
        file_content = "\n".join(import_statements)
        file_content += f"\n\n__all__ = [\n    " + ",\n    ".join(class_names) + "\n]"

        # Write to file
        with open(init_file_path, "w") as f:
            f.write(file_content)


def main():
    """Parse command line arguments and run the schema converter."""
    parser = argparse.ArgumentParser(description="Flags for `schema_to_python.py`.")
    parser.add_argument(
        "-d",
        "--schema_dir",
        action="store",
        default="../schemas/",
        help="Directory where the yaml schema files are stored.",
    )
    parser.add_argument(
        "-o",
        "--output_dir",
        action="store",
        default="generated/",
        help="Directory where the python schema files are written.",
    )
    args = parser.parse_args()

    converter = SchemaConverter()
    converter.process_directory(args.schema_dir, args.output_dir)


if __name__ == "__main__":
    main()
