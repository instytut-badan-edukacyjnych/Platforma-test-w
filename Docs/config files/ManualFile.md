# Manual File

## manual.xml


Manual configuration of task order

```xml
<?xml version="1.0" encoding="utf-8"?>
<manual>
    <script title="manual">
        <task name="P01" />
        <task name="P02" />
        <task name="P03" />
        <task name="P04" />
        <task name="P05" />
    </script>
</manual>
```


| Element | Attribute | Description | Comments | Possible values |
|:-------:|:---------:|:-----------:|:--------:|:---------------:|
| script  | title     | tile of task list| required| manual      |
|task     | name      | name of existing task | required | name of existing task|
