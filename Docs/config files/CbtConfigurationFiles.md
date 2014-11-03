# Cbt configuration files structure
##cbt.xml

```xml
<?xml version="1.0" encoding="utf-8"?>
<cbt name="CBT" order="one_by_one" theta_eps="0.001"></cbt>
```
| Element | Attribute | Description | Comments | Possible values |
|:-------:|:---------:|:-----------:|:--------:|:---------------:|
|cbt      | name      | Name of algorithm| required| string      |
|cbt      | order     | task order  | required | mix, one_by_one |
|cbt      | theta_eps | Used to calculate examinee skill level  | required| double |


```xml
<?xml version="1.0" encoding="utf-8"?>
<cbt-data>
    <script title="badanie">
        <task name="020" />
        <task name="336" />
        <task name="145" />
        <task name="391" />
        <task name="008" />
        <task name="435" />
        <task name="050" />
        <task name="442" />
        <task name="001" />
        <task name="443" />
    </script>
</cbt-data>

```

| Element | Attribute | Description | Comments | Possible values |
|:-------:|:---------:|:-----------:|:--------:|:---------------:|
|script   | title     | name of script| required| string         |
|task     | name      | name of task| required | name of existing task|
