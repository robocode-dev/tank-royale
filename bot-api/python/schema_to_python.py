import argparse
import yaml
from typing import Dict, Any, List, Set, Tuple
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
        self.schema_properties_cache = {}

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
        return type_mapping.get(prop_schema.get('type'), 'object')

    def get_parent_schema_info(self, schema: Dict[str, Any], current_file_path: str) -> Tuple[
        Set[str], Set[str], Dict[str, Any]]:
        """Get the properties and requirements defined in the parent class."""
        if 'extends' not in schema or '$ref' not in schema['extends']:
            return set(), set(), {}

        base_class_ref = schema['extends']['$ref']
        base_file = resolve_reference(base_class_ref)[1]

        cache_key = f"{current_file_path}:{base_file}"
        if cache_key in self.schema_properties_cache:
            return self.schema_properties_cache[cache_key]

        # Use the directory of the current file to resolve the parent file path
        current_dir = Path(current_file_path).parent
        parent_file_path = str(current_dir / base_file)

        try:
            parent_schema = load_yaml(parent_file_path)
            parent_properties = convert_strings_to_snake(parent_schema.get('properties', {}))
            parent_required = set(convert_strings_to_snake2(parent_schema.get('required', [])))

            # If the parent also has a parent, include those properties too
            if 'extends' in parent_schema and '$ref' in parent_schema['extends']:
                grandparent_props, grandparent_required, grandparent_schema = self.get_parent_schema_info(parent_schema,
                                                                                                          parent_file_path)
                parent_properties.update(grandparent_schema)
                parent_required.update(grandparent_required)

            result = (set(parent_properties.keys()), parent_required, parent_properties)
            self.schema_properties_cache[cache_key] = result
            return result
        except FileNotFoundError:
            print(f"Warning: Could not find parent schema file: {parent_file_path}")
            return set(), set(), {}

    def generate_class(self, class_name: str, schema: Dict[str, Any], file_path: str) -> str:
        description = schema.get('description', '')

        if not 'extends' in schema and 'type' in schema and isinstance(schema['type'],
                                                                       str) and 'properties' not in schema:
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

        # Get parent properties and requirements
        parent_properties, parent_required, parent_schema = self.get_parent_schema_info(schema, file_path)

        # Separate required and optional parameters
        required_params = []
        optional_params = []

        # Add all parameters, including those from parent
        all_properties = properties.copy()
        if parent_schema:
            all_properties.update(parent_schema)

        for prop_name, prop_schema in all_properties.items():
            python_name = prop_name
            prop_type = self.get_python_type(prop_schema, file_path)

            if prop_name in required or prop_name in parent_required:
                required_params.append(f'{python_name}: {prop_type}')
            else:
                optional_params.append(f'{python_name}: {prop_type} = None')

        # Combine parameters with required first, then optional
        init_params = required_params + optional_params
        params_str = ', '.join(init_params)

        init_body = [f'    def __init__(self{", " + params_str if params_str else ""}):']

        # Add validation for required parameters
        all_required = set(required).union(parent_required)
        for prop_name in all_required:
            init_body.append(f'        if {prop_name} is None:')
            init_body.append(f'            raise ValueError("The \'{prop_name}\' parameter must be provided.")')

        # Pass parent properties to super().__init__
        if base_classes:
            super_params = []
            for param in init_params:
                param_name = param.split(':')[0]
                if param_name in parent_properties:
                    super_params.append(param_name)

            if super_params:
                super_call = f"        super().__init__({', '.join(super_params)})"
                init_body.append(super_call)

        # Only set properties defined in this class (not parent)
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
        imports = []

        if file_path in self.class_dependencies:
            for dep_file in self.class_dependencies[file_path]:
                module_name = sanitize_file_name(dep_file)
                class_name = sanitize_class_name(dep_file)
                imports.append(f'from .{module_name} import {class_name}')

        return imports

    def generate_dunder_init_py(self, output_dir: str, sub_modules: list[str]) -> None:
        dunder_init_py_path = Path(output_dir) / "__init__.py"
        file_content = '\n'.join(f"from .{m} import *" for m in sub_modules)
        with open(dunder_init_py_path, 'w') as f:
            f.write(file_content)

    def process_directory(self, directory_path: str, output_dir: str) -> None:
        self.output_dir = Path(output_dir)
        self.output_dir.mkdir(parents=True, exist_ok=True)

        yaml_files = list(Path(directory_path).glob('**/*.yaml'))
        yaml_files.extend(Path(directory_path).glob('**/*.yml'))

        sub_modules = []
        for file_path in yaml_files:
            class_code = self.process_schema_file(str(file_path))

            if class_code:
                module_name = f"{sanitize_file_name(file_path)}"
                sub_modules.append(module_name)

                output_file = self.output_dir / f"{module_name}.py"

                content = ['"""', f'Generated Python class from {file_path.name}',
                           'This file is auto-generated. Do not edit manually.', '"""', '']

                content.extend(self.generate_import_statements(str(file_path)))
                content.append('')
                content.append(class_code)

                with open(output_file, 'w') as f:
                    f.write('\n'.join(content))
        
        self.generate_dunder_init_py(output_dir=output_dir, sub_modules=sub_modules)


def main():
    parser = argparse.ArgumentParser(description="Flags for `schema_to_python.py`.")
    parser.add_argument("-d", "--schema_dir", action="store", default='../schemas/', help="Directory where the yaml schema files are stored.")
    parser.add_argument("-o", "--output_dir", action="store", default='generated/', help="Directory where the python schema files are written.")
    args = parser.parse_args()

    converter = SchemaConverter()
    converter.process_directory(args.schema_dir, args.output_dir)


if __name__ == '__main__':
    main()
