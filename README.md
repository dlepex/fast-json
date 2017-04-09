# fast-json

*fast-json* is a modified fork of *dsl-json* https://github.com/ngs-doo/dsl-json with a SOLE PURPOSE to speed up *serialization* of json objects, which were serialized with Jackson before.

### To speed JSON serialization:
- implement FastJsonSerializable#serializeUnwrapped on your *Jackson* pojo 
- generate bodies of serializeUnwrapped() methods using  *FastJsonSerGenerator* (e.g. in IDE console or in tests class - this is not automated and must be done by hand!). *FastJsonSerGenerator* supports some Jackson annotations: JsonProperty, JsonIgnore, JsonUnwrapped. 


Only a portion of classes from *dsl-json* are used - mostly it is *JsonWriter* which was refactored (methods added, renamed) to be more suitable for *FastJsonSerGenerator* - classes like DslJson are not needed at all and may be purged from this lib later.


### Limitations
*FastJsonSerGenerator* only supports fields (public or not).


