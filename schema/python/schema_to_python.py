import yaml
from typing import Dict, Any, List
from pathlib import Path
import re


def camel_to_snake(name: str):
    snake_case = re.sub(r'(?<!^)(?=[A-Z])', '_', name).lower()
    return snake_case

def convert_strings_to_snake(properties: dict[str, Any]):
    new_properties = {}
    for key, value in properties.items():
        new_key = camel_to_snake(key)
        new_properties[new_key] = value
    return new_properties

def convert_strings_to_snake2(items: list[str]):
    new_properties = []
    for item in items:
        new_properties.append(camel_to_snake(item))
    return new_properties

def load_yaml(file_path: str) -> Dict[str, Any]:
    with open(file_path, 'r') as f:
        return yaml.safe_load(f)


def sanitize_file_name(name: str) -> str:
    name = str(Path(name).stem).lower()
    name = name.replace('.schema', '')
    return name.replace('-', '_')


def sanitize_class_name(name: str) -> str:
    name = Path(name).stem
    name = name.replace('.schema', '')
    words = name.replace('-', '_').split('_')
    return ''.join(word.title() for word in words)


def resolve_reference(ref: str) -> tuple[str, str]:
    if not ref:
        return None, None
    ref_path = Path(ref)
    return sanitize_class_name(ref_path.stem), ref_path.name


class SchemaConverter:
    def __init__(self):
        self.processed_schemas = {}
        self.class_dependencies = {}
        self.output_dir = None

    def get_python_type(self, prop_schema: Dict[str, Any], file_path: str) -> str:
        if '$ref' in prop_schema:
            class_name, ref_file = resolve_reference(prop_schema['$ref'])
            if ref_file:
                self.class_dependencies.setdefault(file_path, set()).add(ref_file)
            return class_name

        type_mapping = {
            'string': 'str',
            'integer': 'int',
            'number': 'float',
            'boolean': 'bool',
            'array': 'list',
            'object': 'dict'
        }
        return type_mapping.get(prop_schema.get('type'), 'Any')

    def generate_class(self, class_name: str, schema: Dict[str, Any], file_path: str) -> str:
        description = schema.get('description', '')

        if 'type' in schema and isinstance(schema['type'], str) and 'properties' not in schema:
            python_type = self.get_python_type({'type': schema['type']}, file_path)
            class_lines = [f"class {class_name}:"]

            if description:
                class_lines.extend([f'    """{description}"""', ''])

            class_lines.extend([
                f"    def __init__(self, value: {python_type}):",
                "        if value is None:",
                "            raise ValueError(\"The 'value' parameter must be provided.\")",
                "        self.value = value",
                ""
            ])
            return '\n'.join(class_lines)

        properties = convert_strings_to_snake(schema.get('properties', {}))
        required = convert_strings_to_snake2(schema.get('required', []))

        base_classes = []
        if 'extends' in schema and '$ref' in schema['extends']:
            base_class, base_file = resolve_reference(schema['extends']['$ref'])
            if base_class:
                base_classes.append(base_class)
                if base_file:
                    self.class_dependencies.setdefault(file_path, set()).add(base_file)

        inheritance = f"({', '.join(base_classes)})" if base_classes else ""
        class_lines = [f"class {class_name}{inheritance}:"]

        if description:
            class_lines.extend([f'    """{description}"""', ''])

        # Separate required and optional parameters
        required_params = []
        optional_params = []

        for prop_name, prop_schema in properties.items():
            python_name = prop_name
            prop_type = self.get_python_type(prop_schema, file_path)

            if prop_name in required:
                required_params.append(f'{python_name}: {prop_type}')
            else:
                optional_params.append(f'{python_name}: {prop_type} = None')

        # Combine parameters with required first, then optional
        init_params = required_params + optional_params
        params_str = ', '.join(init_params)

        init_body = [f'    def __init__(self{", " + params_str if params_str else ""}):']

        # Add validation for required parameters
        for prop_name in required:
            init_body.append(f'        if {prop_name} is None:')
            init_body.append(f'            raise ValueError("The \'{prop_name}\' parameter must be provided.")')

        if base_classes:
            super_params = [prop_name.split(':')[0] for prop_name in init_params]
            super_call = f"        super().__init__({', '.join(super_params)})"
            init_body.append(super_call)

        for prop_name in properties:
            init_body.append(f'        self.{prop_name} = {prop_name}')

        class_lines.extend(init_body)
        class_lines.append('')
        return '\n'.join(class_lines)

    def process_schema_file(self, file_path: str) -> str:
        schema_data = load_yaml(file_path)
        class_name = sanitize_class_name(Path(file_path).name)
        return self.generate_class(class_name, schema_data, file_path)

    def generate_import_statements(self, file_path: str) -> List[str]:
        imports = [
            'from typing import Any, List, Dict, Optional',
            'from datetime import datetime, date, time'
        ]

        if file_path in self.class_dependencies:
            for dep_file in self.class_dependencies[file_path]:
                module_name = sanitize_file_name(dep_file)
                class_name = sanitize_class_name(dep_file)
                imports.append(f'from .{module_name} import {class_name}')

        return imports

    def process_directory(self, directory_path: str, output_dir: str):
        self.output_dir = Path(output_dir)
        self.output_dir.mkdir(parents=True, exist_ok=True)

        yaml_files = list(Path(directory_path).glob('**/*.yaml'))
        yaml_files.extend(Path(directory_path).glob('**/*.yml'))

        for file_path in yaml_files:
            class_code = self.process_schema_file(str(file_path))

            if class_code:
                output_file = self.output_dir / f"{sanitize_file_name(file_path)}.py"

                content = ['"""', f'Generated Python class from {file_path.name}',
                           'This file is auto-generated. Do not edit manually.', '"""', '']

                content.extend(self.generate_import_statements(str(file_path)))
                content.append('')
                content.append(class_code)

                with open(output_file, 'w') as f:
                    f.write('\n'.join(content))


def main():
    converter = SchemaConverter()
    converter.process_directory('../schemas/', 'generated/')


if __name__ == '__main__':
    main()