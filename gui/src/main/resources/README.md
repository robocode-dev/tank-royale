# Java Properties File Naming Conventions

This guide explains the naming conventions for Java properties files used in internationalization (i18n) with
`ResourceBundle`.

## Official Documentation

Oracle Java Tutorial:
[Backing a ResourceBundle with Properties Files](https://docs.oracle.com/javase/tutorial/i18n/resbundle/propfile.html)

## Naming Pattern

```
basename_language_COUNTRY_variant.properties
```

### Components

- **basename**: Your application-specific name (e.g., `messages`, `labels`, `strings`)
- **language**: ISO 639-1 two-letter language code (lowercase)
- **COUNTRY**: ISO 3166-1 two-letter country code (UPPERCASE) - optional
- **variant**: Platform or region-specific variant (lowercase) - optional

### Examples

```
messages.properties                 # Default (fallback)
messages_en.properties              # English
messages_en_US.properties           # US English
messages_en_GB.properties           # British English
messages_de.properties              # German
messages_de_CH.properties           # Swiss German
messages_fr.properties              # French
messages_fr_CA.properties           # Canadian French
messages_ca.properties              # Catalan
messages_ca_ES.properties           # Catalan (Spain)
messages_ca_ES_valencia.properties  # Valencian (Spain)
```

## Lookup Order

When `ResourceBundle.getBundle("messages", locale)` is called, Java searches for properties files in the following
order (assuming `Locale("fr", "CA", "UNIX")` and default locale `en_US`):

1. `messages_fr_CA_UNIX.properties`
2. `messages_fr_CA.properties`
3. `messages_fr.properties`
4. `messages_en_US.properties` (default locale)
5. `messages_en.properties`
6. `messages.properties` (base/fallback)

## Best Practices

1. **Always provide a default file**: Create `basename.properties` as the fallback
2. **Use ISO standard codes**: Language codes are lowercase (e.g., `en`, `de`, `fr`)
3. **Country codes are uppercase**: When specifying country variants (e.g., `US`, `GB`, `CA`)
4. **Keep keys consistent**: Keys must match across all localized files
5. **UTF-8 encoding**: Use UTF-8 encoding for properties files containing non-ASCII characters

## Common Language Codes

| Language             | Code  | Example Filename            |
|----------------------|-------|-----------------------------|
| English              | en    | `messages_en.properties`    |
| Spanish              | es    | `messages_es.properties`    |
| French               | fr    | `messages_fr.properties`    |
| German               | de    | `messages_de.properties`    |
| Italian              | it    | `messages_it.properties`    |
| Portuguese           | pt    | `messages_pt.properties`    |
| Catalan/Valencian    | ca    | `messages_ca.properties`    |
| Chinese (Simplified) | zh_CN | `messages_zh_CN.properties` |
| Japanese             | ja    | `messages_ja.properties`    |

## Special Cases

### Catalan and Valencian

Both use language code `ca`:

- `messages_ca.properties` - General Catalan
- `messages_ca_ES.properties` - Catalan in Spain
- `messages_ca_ES_valencia.properties` - Valencian variant

### Chinese

Requires country code for script differentiation:

- `messages_zh_CN.properties` - Simplified Chinese (China)
- `messages_zh_TW.properties` - Traditional Chinese (Taiwan)
