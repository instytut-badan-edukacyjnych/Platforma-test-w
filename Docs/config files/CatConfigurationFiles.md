# Cat configuration files structure
##cat.xml

```xml
<?xml version="1.0" encoding="utf-8"?>
<cat name="CAT" D="1.702" order="mix" step="0.618" theta_eps="0.001">
    <noise base="0.2" />
    <termination criterion="max" value="10" />
    <termination criterion="min" value="5" />
    <termination criterion="deviation" value="0.3" />
</cat>
```

| Element | Attribute | Description | Comments | Possible values |
|:-------:|:---------:|:-----------:|:--------:|:---------------:|
|cat      |name       | Name of algorithm|required| string |
|cat      |D          | IRT factor  | required |double|
|cat      | order     | task order  | required | mix, one_by_one |
|cat      | step     | Used to calculate examinee skill level  | required| double |
|cat      | theta_eps     | Used to calculate examinee skill level  | required| double |
|noise    | base      |Noise used to differ results|required |double|
|termination| criterion| Stop condition type| required| max,min,deviation|
|termination| value   | Stop condition value| required| integer, double|

## cat-data.xml

```xml
<?xml version="1.0" encoding="utf-8"?>
<cat-data>
    <area name="m">
        <scope score="-0.8256">
            <examinee-age from="0" to="71" />
            <examinee-gender value="m" />
        </scope>
        <scope score="-0.8287">
            <examinee-age from="0" to="71" />
            <examinee-gender value="k" />
        </scope>
        <scope score="-0.0564">
            <examinee-age from="72" to="83" />
            <examinee-gender value="m" />
        </scope>
        <scope score="-0.0782">
            <examinee-age from="72" to="83" />
            <examinee-gender value="k" />
        </scope>
        <scope score="0.4972">
            <examinee-age from="84" to="9990000000000000" />
            <examinee-gender value="m" />
        </scope>
        <scope score="0.5413">
            <examinee-age from="84" to="9990000000000000" />
            <examinee-gender value="k" />
            <institution-city value="Gdynia">
            <institution-postal value="81-123">
        </scope>
    </area>
</cat-data>
```

| Element | Attribute | Description | Comments | Possible values |
|:-------:|:---------:|:-----------:|:--------:|:---------------:|
|area     | -----     |area tag     |required,at least one |-----|
|area     | name      |name of area | required | character       |
|scope    | score     |base theta value| required| double        |
|examinee-age|  ----- |scope selector| optional|  -----          |
|examinee-age| from   |examinee age limits| required| integer    |
|examinee-age| to     |examinee age limits| required| integer    |
|examinee-gender|-----|scope selector| optional| -----           |
|examinee-gender|value|gender limits|required| m,k               |
|institution-postal| -----| scope selector| optional| -----      |
|institution-postal| value|postal code selector | required|string|
|institution-city| ------ | scope selector| optional | -----     |
|institution-city| value | city selector| required | string      |
