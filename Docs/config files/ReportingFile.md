# Reporting file structure
##raporting.xml
File existance is optional.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<raporting url="https://8.8.8.8/">
    <researcher>
        <id />
    </researcher>
    <examinee>
        <id />
        <gender />
        <birthday />
    </examinee>
    <institution>
        <id />
    </institution>
    <raport_content>
        <summary />
        <tasks />
    </raport_content>
</raporting>
```

| Element | Attribute | Description | Comments | Possible values | Possible sub tags|
|:-------:|:---------:|:-----------:|:--------:|:---------------:|:----------------:|
|raporting| url       | url where to send report| required| valid url to reporting server|-----|
|researcher| -----    | Elements needed to be included with researcher data in report| optional| -----| id|
|examinee | -----     | Elements needed to be included with examinee data in report|optional|-----|id,gender,birthday|
|institution| -----   | Elements needed to be included with institution data in report|optional|-----|id|
|raport_content| -----| Required parts of report| optional| -----| summary, tasks|
|id      | -----      | Id of element will be send| optional|-----|------|
|gender  | -----      | Gender of element will be send| optional|-----|------|
|birthday| -----      | Birthday of element will be send| optional|-----|------|
|summary | -----      | Summary report will be send|optional |-----|------|
|task    | -----      | Task report will be send |optional |-----|------|
