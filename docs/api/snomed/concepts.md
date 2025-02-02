# SNOMED CT Concept API

## Introduction

**SNOMED CT concepts** represent ideas that are relevant in a clinical setting and have a unique concept identifier 
(a SNOMED CT identifier or **SCTID** for short) assigned to them. The terminology covers a wide set of domains and 
includes concepts that represent parts of the human body, clinical findings, medicinal products and devices, among 
many others. SCTIDs make it easy to refer unambiguously to the described ideas in eg. an Electronic Health Record or 
prescription, while SNOMED CT's highly connected nature allows complex analytics to be performed on aggregated data.

Each concept is associated with human-readable **description**s that help users select the SCTID appropriate for their
use case, as well as **relationship**s that form links between other concepts in the terminology, further clarifying
their intended meaning. The API for manipulating the latter two types of components are covered in sections 
[Descriptions](descriptions.md) and [Relationships](relationships.md), respectively.

The three component types mentioned above (also called **core components**) have a distinct set of attributes which
together form the concept's definition. As an example, each concept includes an attribute (the **definition status**) 
which states whether the definition is sufficiently defined (and so can be computationally processed), or relies on 
a (human) reader to come up with the correct meaning based on the associated descriptions.

Terminology services exposed by Snow Owl allows clients to *create*, *retrieve*, *modify* or *remove* concepts from a 
SNOMED CT code system (concepts that are considered to be already published to consumers can only be removed with an 
administrative operation). Concepts can be retrieved by SCTID or description search terms; results can be further 
constrained via Expression Constraint Language (**ECL** for short) expressions.

## Code system paths

Snow Owl supports importing SNOMED CT content from different sources, allowing eg. multiple national **Extensions** to 
co-exist with the base **International Edition** provided by SNOMED International. Versioned editions can be consulted 
when non-current representations of concepts need to be accessed. Concept authoring and review can also be done in 
isolation.

To achieve this, the underlying terminology repository exposes a branching model (not unlike software development 
branches in Revision Control Systems); requests in the Concept API require a `path` parameter to select the content 
(or **substrate**) the user wishes to work with. The following formats are accepted:

### Absolute branch path

Absolute branch path parameters start with `MAIN` and point to a branch in the backing terminology repository. In the 
following example, all concepts are considered to be part of the substrate that are on branch 
`MAIN/2021-01-31/SNOMEDCT-UK-CL` or any ancestor (ie. `MAIN` or `MAIN/2021-01-31`), unless they have been modified:

```json
GET /snomed-ct/v3/MAIN/2021-01-31/SNOMEDCT-UK-CL/concepts
{
  "items": [
    {
      "id": "100000000",
      "released": true,
      "active": false,
      "effectiveTime": "20090731",
[...]
```

### Relative branch path

Relative branch paths start with a short name identifying a SNOMED CT code system, and are relative to the code 
system's working branch. For example, if the working branch of code system `SNOMEDCT-UK-CL` is configured to 
`MAIN/2021-01-31/SNOMEDCT-UK-CL`, concepts visible on authoring task #100 can be retrieved using the following request:

```json
GET /snomed-ct/v3/SNOMEDCT-UK-CL/100/concepts
```

An alternative request that uses an absolute path would be the following:

```json
GET /snomed-ct/v3/MAIN/2021-01-31/SNOMEDCT-UK-CL/100/concepts
```

An important difference is that the relative `path` parameter tracks the working branch specified in the code 
system's settings, so requests using relative paths do not need to be adjusted when a code system is upgraded to a
more recent International Edition.

### Path range

The substrate represented by a path range consists of concepts that were created or modified between a starting and
ending point, each identified by an absolute branch path (relative paths are not supported). The format of a path range 
is `fromPath...toPath`.

To retrieve concepts authored or edited following version 2020-08-05 of code system SNOMEDCT-UK-CL, the following path
expression should be used:

```json
GET /snomed-ct/v3/MAIN/2019-07-31/SNOMEDCT-UK-CL/2020-08-05...MAIN/2021-01-31/SNOMEDCT-UK-CL/concepts
```

The result set includes the ones appearing or changing between versions 2019-07-31 and 2021-01-31 of the International 
Edition; if this is not desired, additional constraints can be added to exclude them.

### Path with timestamp

To refer to a branch state at a specific point in time, use the `path@timestamp` format. The timestamp is an integer 
value expressing the number of milliseconds since the UNIX epoch, 1970-01-01 00:00:00 UTC, and corresponds to "wall 
clock" time, not component effective time. As an example, if the SNOMED CT International version 2021-07-31 is imported 
on 2021-09-01 13:50:00 UTC, the following request to retrieve concepts will not include any new or changed concepts 
appearing in this release:

```json
GET /snomed-ct/v3/MAIN@1630504199999/concepts
```

Both absolute and relative paths are supported in the `path` part of the expression.

### Branch base point

Concept requests using a branch base point reflect the state of the branch at its beginning, before any changes on it
were made. The format of a base path is `path^` (only absolute paths are supported):

```json
GET /snomed-ct/v3/MAIN/2019-07-31/SNOMEDCT-UK-CL/101^/concepts
```

Returned concepts include all additions and modifications made on SNOMEDCT-UK-CL's working branch, up to point where 
task #101 starts; neither changes committed to the working branch after task #101, nor changes on task #101 itself are
reflected in the result set.

## Resource format

A concept resource without any expanded properties looks like the following:

```json
{
  "id": "138875005",
  "released": true,
  "active": true,
  "effectiveTime": "20020131",
  "moduleId": "900000000000207008",
  "iconId": "snomed_rt_ctv3",
  "definitionStatus": {
    "id": "900000000000074008"
  },
  "subclassDefinitionStatus": "NON_DISJOINT_SUBCLASSES",
  "ancestorIds": [],
  "parentIds": [
    "-1"
  ],
  "statedAncestorIds": [],
  "statedParentIds": [
    "-1"
  ],
  "definitionStatusId": "900000000000074008"
}
```

### Properties

The resource includes all RF2 properties that are defined in SNOMED International's
[Release File Specification](https://confluence.ihtsdotools.org/display/DOCRELFMT/4.2.1+Concept+File+Specification)🌎:

- `id`
- `effectiveTime`
- `active`
- `moduleId`
- `definitionStatusId`

It also contains the following supplementary information:

- `parentIds`, `ancestorIds`

These arrays hold a set of SCTIDs representing the concept's direct and indirect ancestors in the 
inferred taxonomy. The (direct) parents array contains all `destinationId`s from active and inferred IS A 
relationships where the `sourceId` matches this concept's SCTID, while the ancestor array contains all SCTIDs 
taken from the parent and ancestor array of direct parents. The arrays are sorted by SCTID. A value of `-1` means 
that the concept is a **root concept** that does not have any concepts defined as its parent. Typically, this only 
applies to `138875005|Snomed CT Concept|` in SNOMED CT content.

See the following example response for a concept placed deeper in the tree:

```json
GET /snomed-ct/v3/MAIN/concepts/425758004 // Diagnostic blood test
{
  [...]
  "ancestorIds": [
    "-1",        // Special value for taxonomy root
    "15220000",  // Laboratory test
    "71388002",  // Procedure
    "108252007", // Laboratory procedure (not pictured below)
    "128927009", // Procedure by method
    "138875005", // SNOMED CT Concept
    "362961001", // Procedure by intent
    "386053000"  // Evaluation procedure
  ],
  "parentIds": [
    "103693007", // Diagnostic procedure
    "396550006"  // Blood test
  ],
  [...]
}
```

Compare the output with a rendering from a user interface, where the concept appears in two different places after 
exploring alternative routes in the hierarchy. Parents are marked with blue, while ancestors are highlighted with 
orange:

![Parents and ancestors](images/parents_ancestors.png)

- `statedParentIds`, `statedAncestorIds`

Same as the above, but for the stated taxonomy view.

- `released`

A boolean value indicating whether this concept was part of at least one SNOMED CT release. New concepts 
start with a value of `false`, which is set to `true` as part of the code system versioning process. Released 
concepts can only be deleted by an administrator.

- `iconId`

A descriptive key for the concept's icon. The icon identifier typically corresponds to the lowercase, 
underscore-separated form of the [hierarchy tag](https://confluence.ihtsdotools.org/display/DOCGLOSS/hierarchy+tag)🌎 
contained in each concept's Fully Specified Name (or **FSN** for short). The following keys are currently expected 
to appear in responses (subject to change):

`administration_method`,
`assessment_scale`,
`attribute`,
`basic_dose_form`,
`body_structure`,
`cell`,
`cell_structure`,
`clinical_drug`,
`disorder`,
`disposition`,
`dose_form`,
`environment`,
`environment_location`,
`ethnic_group`,
`event`,
`finding`,
`geographic_location`,
`inactive_concept`,
`intended_site`,
`life_style`,
`link_assertion`,
`linkage_concept`,
`medicinal_product`,
`medicinal_product_form`,
`metadata`,
`morphologic_abnormality`,
`namespace_concept`,
`navigational_concept`,
`observable_entity`,
`occupation`,
`organism`,
`owl_metadata_concept`,
`person`,
`physical_force`,
`physical_object`,
`procedure`,
`product`,
`product_name`,
`qualifier_value`,
`racial_group`,
`record_artifact`,
`regime_therapy`,
`release_characteristic`,
`religion_philosophy`,
`role`,
`situation`,
`snomed_rt_ctv3`,
`social_concept`,
`special_concept`,
`specimen`,
`staging_scale`,
`state_of_matter`,
`substance`,
`supplier`,
`transformation`,
`tumor_staging`,
`unit_of_presentation`

In the metadata hierarchy, the use of a hierarchy tag alone would not distinguish concepts finely enough, as lots of 
them will have eg. "foundation metadata concept" set as their tag. In these cases, concept identifiers may be used as 
the icon identifier.

- `subclassDefinitionStatus`

{% hint style="warning" %}
**Currently unsupported.** Indicates whether a parent concept's direct descendants form a 
[disjoint union](https://www.w3.org/TR/owl2-syntax/#Disjoint_Union_of_Class_Expressions)🌎 in OWL 2 terms; when set to 
`DISJOINT_SUBCLASSES`, child concepts are assumed to be pairwise disjoint and together cover all possible cases of 
the parent concept.

The default value is `NON_DISJOINT_SUBCLASSES` where no such assumption is made.
{% endhint %}

### Property expansion

Core component information related to the current concept can be attached to the response by using the `expand` query 
parameter, allowing clients to retrieve more data in a single roundtrip. Property expansion runs the necessary requests 
internally, and attaches results to the original response object. 

Expand options are expected to appear in the form of 
`propertyName1(option1: value1, option2: value2, expand(...)), propertyName2()` where:

- `propertyNameN` stands for the property to expand;
- `optionN: valueN` are key-value pairs providing additional filtering for the expanded property;
- optionally, `expand`s can be nested, and the options will apply to the components returned under the parent property;
- when no expand options are given, an empty set of `()` parentheses need to be added after the property name.

Supported expandable property names are:

#### `referenceSet()`

Expands reference set metadata and content, available on 
[identifier concepts](https://confluence.ihtsdotools.org/display/DOCRFSPG/4.2.1.+Reference+Set+Identification)🌎.

If a corresponding reference set was already created for an identifier concept (a subtype of 
`900000000000455006|Reference set`), information about the reference set will appear in the response:

```json
GET /snomed-ct/v3/MAIN/concepts/900000000000497000?expand=referenceSet() // CTV3 simple map
{
  "id": "900000000000497000",
  "active": true,
  [...]  
  "referenceSet": {
    "id": "900000000000497000",
    "released": true,
    "active": true,
    "effectiveTime": "20020131",
    "moduleId": "900000000000012004",
    "iconId": "900000000000496009",
    "type": "SIMPLE_MAP",                    // Reference set type
    "referencedComponentType": "concept",    // Referenced component type
    "mapTargetComponentType": "__UNKNOWN__"  // Map target component type 
                                             // (applicable to map type reference sets only)
  },
  [...]
}
```

Note that the response object for property `referenceSet` can also be retrieved directly using the 
[Reference Sets API](refsets.md).

To retrieve reference set members along with the reference set in a single request, use a nested `expand` property 
named `members`:

```json
GET /snomed-ct/v3/MAIN/concepts/900000000000497000?expand=referenceSet(expand(members()))
{
  "id": "900000000000497000",
  [...]
  "referenceSet": {
    [...]
    "type": "SIMPLE_MAP",
    "referencedComponentType": "concept",
    "mapTargetComponentType": "__UNKNOWN__",
    "members": {
      "items": [
        {
          "id": "00000193-e889-4d3f-b07f-e0f45eb77940",
          "released": true,
          "active": true,
          "effectiveTime": "20190131",
          "moduleId": "900000000000207008",
          "iconId": "776792002",
          "referencedComponent": {
            "id": "776792002"
          },
          "refsetId": "900000000000497000", // Reference set ID matches the identifier concept's ID 
                                            // for all members of the reference set
          "referencedComponentId": "776792002",
          "mapTarget": "XV8E7"
        },
        [...]
      ],
      "searchAfter": "AoE_BTAwMDcyYWIzLWM5NDgtNTVhYy04MTBkLTlhOGNhMmU5YjQ5Yg==",
      "limit": 50,
      "total": 481508
    }
  },
}
```

Reference set members can also be fetched via the [SNOMED CT Reference Set Member API](refsets.md).

#### `preferredDescriptions()`

Expands descriptions with preferred acceptability.

Returns all active descriptions that have at least one active language reference set member with an acceptabilityId of 
`900000000000548007|Preferred|`, in compact form, along with the concept. Preferred descriptions are frequently used 
on UIs when a display label is required for a concept.

This information is also returned when expand options `pt()` or `fsn()` (described later) are present.

```json
GET /snomed-ct/v3/MAIN/2011-07-31/concepts/86299006?expand=preferredDescriptions()
{
  "id": "86299006", // Concept SCTID
  [...]
  "preferredDescriptions": {
    "items": [
      {
        "id": "828532012",                        // Description SCTID
        "term": "Tetralogy of Fallot (disorder)", // Description term
        "concept": {
          "id": "86299006"
        },
        "type": {
          "id": "900000000000003001"
        },
        "typeId": "900000000000003001",           // Type: Fully Specified Name
        "conceptId": "86299006",                  // "conceptId" matches the returned concept's SCTID
        "acceptability": {
          "900000000000509007": "PREFERRED",      // Acceptability in reference set "US English"
          "900000000000508004": "PREFERRED"       // Acceptability in reference set "GB English"
        }
      },
      {
        "id": "143123019",
        "term": "Tetralogy of Fallot",
        "concept": {
          "id": "86299006"
        },
        "type": {
          "id": "900000000000013009"
        },
        "typeId": "900000000000013009",           // Type: Synonym
        "conceptId": "86299006",
        "acceptability": {
          "900000000000509007": "PREFERRED",
          "900000000000508004": "PREFERRED"
        }
      }
    ],
    "limit": 2,
    "total": 2
  },
  [...]
}
```

#### `semanticTags()`

Returns hierarchy tags extracted from FSNs.

An array containing the hierarchy tags from all Fully Specified Name-typed descriptions of the concept is added as an 
expanded property if this option is present:

```json
GET /snomed-ct/v3/concepts/MAIN/103981000119101?expand=preferredDescriptions(),semanticTags()
{
  "id": "103981000119101",
  "released": true,
  "active": true,
  "effectiveTime": "20200131",
  "preferredDescriptions": {
    "items": [
      {
        "id": "3781804016",
        "term": "Proliferative retinopathy following surgery due to diabetes mellitus (disorder)",
        [...]
      },
      [...]
    ]
  }
  [...]
  "semanticTags": [ "disorder" ], // Extracted from the Fully Specified Name; see term above
  [...]
}
```

#### `inactivationProperties()` 

Collects information from concept inactivation indicator and historical association reference set members referencing 
this concept.

Members of `900000000000489007|Concept inactivation indicator attribute value reference set|` and subtypes of
`900000000000522004 |Historical association reference set|` hold information about a reason a concept is being retired 
in a release, as well as suggest potential replacement(s) for future use.

The concept stating the reason for inactivation is placed under `inactivationProperties.inactivationIndicator.id` 
(a short-hand property exists without an extra nesting, named `inactivationProperties.inactivationIndicatorId`). It is 
expected that only a single active inactivation indicator exists for an inactive concept.

Historical associations are returned under the property `inactivationProperties.associationTargets` as an array of 
objects. Each object includes the identifier of the historical association reference set and the target component 
identifier, in the same manner as described above &ndash; as an object with a single `id` property and as a string
value.

```json
GET /snomed-ct/v3/concepts/MAIN/99999003?expand=inactivationProperties()
{
  "id": "99999003",
  "active": false,
  "effectiveTime": "20090731",
  [...]
  "inactivationProperties": {
    "inactivationIndicator": {
      "id": "900000000000487009"
    },
    "associationTargets": [
      {
        "referenceSet": {
          "id": "900000000000524003"
        },
        "targetComponent": {
          "id": "416516009"
        },
        "referenceSetId": "900000000000524003",     // MOVED TO association reference set
        "targetComponentId": "416516009"            // Extension Namespace 1000009
      }
    ],
    "inactivationIndicatorId": "900000000000487009" // Moved elsewhere
  },
  [...]
}
```

{% hint style="warning" %}
While most object values where a single `id` key is present indicate that the property can be expanded to a full
resource representation, this is currently **not supported** for inactivation properties; an expand option of 
`inactivationProperties(expand(inactivationIndicator()))` will not retrieve additional data for the indicator concept.
{% endhint %}

#### `members()`

Expands reference set members referencing this concept.

Note that this is different from reference set member expansion on a reference set, ie. 
`referenceSet(expand(members()))`, as this option will return reference set members where the `referencedComponentId` property matches the concept SCTID, from multiple reference sets (if permitted by other expand options). Inactivation 
and historical association members can also be returned here, in their entirety (as opposed to the summarized form 
described in `inactivationProperties()` above).

Reference set members can also be fetched in a "standalone" fashion via the 
[SNOMED CT Reference Set Member API](refsets.md).

Compare the output with the one returned when inactivation indicators were expanded. The last two reference set members 
correspond to the historical association and the inactivation reason, respectively:

```json
GET /snomed-ct/v3/concepts/MAIN/99999003?expand=members()
{
  "id": "99999003",
  [...]
  "members": {
    "items": [
      {
        "id": "f2b12ff9-794a-5a05-8027-88f0492f3766",
        "released": true,
        "active": true,
        "effectiveTime": "20020131",
        "moduleId": "900000000000207008",
        "iconId": "99999003",
        "referencedComponent": {
          "id": "99999003"
        },
        "refsetId": "900000000000497000",    // CTV3 simple map
        "referencedComponentId": "99999003", // all referencedComponentIds match the concept's SCTID
        "mapTarget": "XUPhG"                 // additional properties are displayed depending on the
                                             // reference set type
      },
      {
        "id": "5e9787df-11af-54ed-ae92-0ea3bc83f2ac",
        "released": true,
        "active": true,
        "effectiveTime": "20090731",
        "moduleId": "900000000000207008",
        "iconId": "99999003",
        "referencedComponent": {
          "id": "99999003"
        },
        "refsetId": "900000000000524003",    // MOVED TO association reference set
        "referencedComponentId": "99999003",
        "targetComponentId": "416516009"     // Extension Namespace 1000009
      },
      {
        "id": "9ffd949a-27d0-5811-ad48-47ff43e1bded",
        "released": true,
        "active": true,
        "effectiveTime": "20090731",
        "moduleId": "900000000000207008",
        "iconId": "99999003",
        "referencedComponent": {
          "id": "99999003"
        },
        "refsetId": "900000000000489007",    // Concept inactivation indicator reference set
        "referencedComponentId": "99999003",
        "valueId": "900000000000487009"      // Moved elsewhere
      }
    ],
    "limit": 3,
    "total": 3
  },
  [...]
}
```

The following expand options are supported within `members(...)`:

- `active: true | false`

Controls whether only active or inactive reference set members should be returned.

- `refSetType: "{type}" | [ "{type}"(,"{type}")* ]`

The reference set type(s) as a string, to be included in the expanded output; when multiple types are accepted, values must be enclosed in square brackets and separated by a comma.

- `expand(...)`

Allows nested expansion of reference set member properties.

Allowed reference set type constants are (these are described in the 
[Reference Set Types](https://confluence.ihtsdotools.org/display/DOCRFSPG/5.+Reference+Set+Types)🌎 section of SNOMED 
International's "Reference Sets Practical Guide" and the
[Reference Set Types](https://confluence.ihtsdotools.org/display/DOCRELFMT/5.2+Reference+Set+Types)🌎 section of 
"Release File Specification" in more detail):

- `SIMPLE` - simple type
- `SIMPLE_MAP` - simple map type
- `LANGUAGE` - language type
- `ATTRIBUTE_VALUE` - attribute-value type
- `QUERY` - query specification type
- `COMPLEX_MAP` - complex map type
- `DESCRIPTION_TYPE` - description type
- `CONCRETE_DATA_TYPE` - concrete data type (vendor extension for representing concrete values in Snow Owl)
- `ASSOCIATION` - association type
- `MODULE_DEPENDENCY` - module dependency type
- `EXTENDED_MAP` - extended map type
- `SIMPLE_MAP_WITH_DESCRIPTION` - simple map type with map target description (vendor extension for storing a 
  descriptive label with map targets, suitable for display)
- `OWL_AXIOM` - OWL axiom type
- `OWL_ONTOLOGY` - OWL ontology declaration type
- `MRCM_DOMAIN` - MRCM domain type
- `MRCM_ATTRIBUTE_DOMAIN` - MRCM attribute domain type
- `MRCM_ATTRIBUTE_RANGE` - MRCM attribute range type
- `MRCM_MODULE_SCOPE` - MRCM module scope type
- `ANNOTATION` - annotation type
- `COMPLEX_BLOCK_MAP` - complex map with map block type (added for national extension support)

See the following example for combining reference set member status filtering and reference set type restriction:

```json
GET /snomed-ct/v3/concepts/MAIN/99999003?expand=members(active:true, refSetType:["ASSOCIATION","ATTRIBUTE_VALUE"])
{
  "id": "99999003",
  [...]
  "members": {
    [
      {
        "id": "5e9787df-11af-54ed-ae92-0ea3bc83f2ac",
        "released": true,
        "active": true,
        "effectiveTime": "20090731",
        "moduleId": "900000000000207008",
        "iconId": "99999003",
        "referencedComponent": {
          "id": "99999003"
        },
        "refsetId": "900000000000524003",    // MOVED TO association reference set
        "referencedComponentId": "99999003",
        "targetComponentId": "416516009"     // Extension Namespace 1000009
      },
      {
        "id": "9ffd949a-27d0-5811-ad48-47ff43e1bded",
        "released": true,
        "active": true,
        "effectiveTime": "20090731",
        "moduleId": "900000000000207008",
        "iconId": "99999003",
        "referencedComponent": {
          "id": "99999003"
        },
        "refsetId": "900000000000489007",    // Concept inactivation indicator reference set
        "referencedComponentId": "99999003",
        "valueId": "900000000000487009"      // Moved elsewhere
      }
    ],
    "limit": 2,
    "total": 2
  },
  [...]
}
```

## Operations

### Retrieve single concept by ID

A GET request that includes a concept identifier as its last path parameter will return information about the concept 
in question:

```json
GET /snomed-ct/v3/MAIN/2019-07-31/concepts/138875005
```
