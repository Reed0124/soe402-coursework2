# UML sources

The two PlantUML files in this folder are the source of the class diagram submitted as
`UML_v1_Group7.pdf`:

| File | Page |
|---|---|
| `uml_p1.puml` | Domain model: cards, characters, effects |
| `uml_p2.puml` | Engine, events and logging |

Render them with PlantUML (Graphviz is not required, the files use the built-in Smetana layout):

```
java -jar plantuml.jar -tsvg uml_p1.puml uml_p2.puml
```

The exported PDF wraps the two SVGs in an A3 landscape layout together with a cover page and
a notation legend. When the code changes, update these files first and re-export, so that the
diagram and the implementation never drift apart.
