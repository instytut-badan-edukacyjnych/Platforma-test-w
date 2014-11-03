# Task Creation

This document describes how to create particular tasks in tasks suites.

## Described tasks types

1. [Task Group](#task-group)
1. [Look Task](#look-task)
1. [Choose Nx Task](#choose-nx-task)
1. [Choose 1x Task](#choose-1x-task)
1. [Mark Flex Nx Task](#mark-flex-nx-task)
1. [Mark Nx Task](#mark-nx-task)
1. [Move N Task](#move-n-task)
1. [Move Task](#move-task)
1. [Clock Task](#clock-task)
1. [Select Task](#select-task)
1. [Multi Select Task](#multi-select-task)

You can create own task by extending **BaseTask** class and
putting it in **pl.edu.ibe.loremipsum.task** package.

You can use above listed tasks as example.

## Arbiters

| Element | Description |
|:-------:|:-----------:|
|External | Leaves task mark completely to researcher|
|Puzzle   | Marks puzzle tasks|
|Place    | Gives mark depending on elements locations|
|Count    | Gives mark depending on number of elements|
|Time select| Gives mark depending on task completion time|
|Number   | Gives mark depending on number of elements|
|Position | Gives mark depending on elements locations|
|Order    |Gives mark depending on elements order|
|Location | Gives mark depending on elements locations|
|Clock    | Gives mark depending selected time on clock|
|Select   | Gives mark depending on selected item|

You can create own arbiters by extending **BaseArbiter** and
putting it in **pl.edu.ibe.loremipsum.arbiter** package.

You can use above listed tasks as example.

## Common file structure

```xml
<?xml version="1.0" encoding="utf-8"?>
<details name="TASK NAME">
  <task class="TASK CLASS"  ..... />
</details>

```

| Element | Attribute | Description | Comments | Possible values |
|:-------:|:---------:|:-----------:|:--------:|:---------------:|
|details  |  -----    | Container for task elements| required | -----|
|details  | name      | name of task|required  | string          |
|task     |  -----    | Task description | required at least once| -----|
|task     | class     | Task class. May require some additional attributes in task tag depending on this value| required| Task type name |
|task     | arbiter   | Arbiter for this class| optional  | arbiter name|
|task     | sound     | Sound for this task | optional | sound file name|
|task     | property  | Properties of this task.| optional | see [Property attribute](#property-attribute)|
|task     | main      | Main image of this task| optional | image file name|
|task     | place     | Drop place mask for move tasks | optional | image file name|
|task     | marker    | Marker mask for select tasks | optional | image file name|
|task     |  bgcolor  | Background color for task| optional | color in hex format|




### Property attribute
Avalible flags:

| Flag | Description |
|:----:|:-----------:|
| E    | Expand marks button|
| M    | Enable voice recording in this task|
| Q    | Disable auto voice recording |
| P    | Enable photo capture |
| 2    | Mark range 0-1|
| 3    | Mark range 0-1-2|
| O    | Mark range 0 - unlimited. Note that this flag requires range value from task.xml file and corresponding number of  mark modifiers. |
| R    | Reload task button |
| S    | Step to next task button. More: [Task Group](#task-group)|
| C    | Replay command|
| N    | Open next task after sound |
| A    | This and next task is alternative|
| L    | Loop point in sequence tasks|
| X    | Additional sound for next task in sequence|
| I    | Continue sequence after answer|
| U    | Pull items to selected localization|
| H    | Align base line|
| B    | Align brink|
| D    | After release item is deselected|


## Task Group
Example:

```xml
<?xml version="1.0" encoding="utf-8"?>
<details name="001">
    <task class="TaskGroup" />
    <!-- example next task -->
    <task bgcolor="#FFF2F2F2" class="LookTask" main="001.png" property="SL" sound="001a.m4a" />
    <task bgcolor="#FFF2F2F2" class="LookTask" main="002.png" property="E" sound="002a.m4a" />
</details>
```

First task tag with attribute *TaskGroup* informs that a sequence of tasks is included in this file.
All next tasks can be various types. Note that to allow user to step into next task in sequence you **must** include at least *S* flag in property attribute of task.
Task Group **does not** require any other additional attributes.

## Look Task

Example:

```xml
<?xml version="1.0" encoding="utf-8"?>
<details name="001">
    <task bgcolor="#FFF2F2F2" class="LookTask" main="001.png" property="SL" sound="001a.m4a" />
</details>
```

Task specific elements:

| Element | Description | Comments |
|:---------:|:-----------:|:--------:|
| main      | background image| required|
| sound     | background sound | optional |

## Choose Nx Task
Example:

```xml
<?xml version="1.0" encoding="utf-8"?>
<details name="P01">
    <task bgcolor="#FFF2F2F2" class="ChooseNxTask" main="P01.png" number="8" answer="A B" >
        <field name="A" mask="P01mA.png" />
        <field name="B" mask="P01mB.png" />
        <field name="C" mask="P01mC.png" />
        <field name="D" mask="P01mD.png" />
        <field name="E" mask="P01mE.png" />
        <field name="F" mask="P01mF.png" />
        <field name="G" mask="P01mG.png" />
        <field name="H" mask="P01mH.png" />
    </task>
</details>
```

Task specific elements:

| Element | Description | Comments |
|:---------:|:-----------:|:--------:|
| number    | Number of fields to select| required|
| answer    | Correct answers names| required|
|field      | Field to select| required **number attribute value** times|
|name       | Field name | required|
| mask      | Selected field mask. Note that size of mask should be same size as main image. | required|


## Choose 1x Task

```xml
<?xml version="1.0" encoding="utf-8"?>
<details name="001">
    <task answer="A" arbiter="SelectArbiter" bgcolor="#FFF2F2F2" class="Choose1xTask" main="001.png"
        number="5" property="E2C" sound="001b.m4a">
        <field name="A" mask="001mA.png" />
        <field name="B" mask="001mB.png" />
        <field name="C" mask="001mC.png" />
        <field name="D" mask="001mD.png" />
        <field name="E" mask="001mE.png" />
    </task>
</details>

```
Task specific elements:

| Element | Description | Comments |
|:---------:|:-----------:|:--------:|
| number    | Number of fields to select| required|
| answer    | Correct answer name| required|
|field      | Field to select | required **number attribute value** times|
|name       | Field name | required|
| mask      | Selected field mask. Note that size of mask should be same size as main image. | required|

## Select Task

```xml
<?xml version="1.0" encoding="utf-8"?>
<details name="001">
    <task answer="C" arbiter="SelectArbiter" bgcolor="#FFF2F2F2" class="SelectTask" main="001.png"
        marker="001m.png" number="4" property="E2C" sound="001.m4a">
        <field name="A" x="942" y="22" />
        <field name="B" x="942" y="211" />
        <field name="C" x="942" y="398" />
        <field name="D" x="942" y="587" />
    </task>
</details>

```

Task specific elements:

| Element | Description | Comments |
|:---------:|:-----------:|:--------:|
| number    | Number of fields to select| required|
| answer    | Correct answer name| required|
|field      | Field to select | required **number attribute value** times|
|name       | Field name | required|
| x         | X element coordinate | required|
| y         | Y element coordinate | required|

## Mark Flex Nx Task

```xml
<?xml version="1.0" encoding="utf-8"?>
<details name="002">
    <task answer="A D E" arbiter="SelectArbiter" bgcolor="#FFF2F2F2" class="MarkFlexNxTask" main="002.png"
        marker="002m.png" number="6" property="E2C" sound="002.m4a">
        <field name="A" px="370" py="300" x="70" y="95" />
        <field name="B" px="400" py="616" x="102" y="407" />
        <field name="C" px="714" py="380" x="415" y="117" />
        <field name="D" px="700" py="720" x="408" y="517" />
        <field name="E" px="1050" py="330" x="757" y="126" />
        <field name="F" px="1040" py="640" x="748" y="436" />
    </task>
</details>
```

Task specific elements:

| Element | Description | Comments |
|:---------:|:-----------:|:--------:|
| number    | Number of fields to select| required|
| answer    | Correct answers names| required|
|field      | Field to select | required **number attribute value** times|
|name       | Field name | required|
| x         | Begin X element coordinate | required|
| y         | Begin Y element coordinate | required|
| px         | End X element coordinate | required|
| py         | End  Y element coordinate | required|

## Mark Flex 1x Task

```xml
<?xml version="1.0" encoding="utf-8"?>
<details name="002">
    <task answer="A" arbiter="SelectArbiter" bgcolor="#FFF2F2F2" class="MarkFlex1xTask" main="002.png"
        marker="002m.png" number="6" property="E2C" sound="002.m4a">
        <field name="A" px="370" py="300" x="70" y="95" />
        <field name="B" px="400" py="616" x="102" y="407" />
        <field name="C" px="714" py="380" x="415" y="117" />
        <field name="D" px="700" py="720" x="408" y="517" />
        <field name="E" px="1050" py="330" x="757" y="126" />
        <field name="F" px="1040" py="640" x="748" y="436" />
    </task>
</details>
```

Task specific elements:

| Element | Description | Comments |
|:---------:|:-----------:|:--------:|
| number    | Number of fields to select| required|
| answer    | Correct answers names| required|
|field      | Field to select | required **number attribute value** times|
|name       | Field name | required|
| x         | Begin X element coordinate | required|
| y         | Begin Y element coordinate | required|
| px         | End X element coordinate | required|
| py         | End  Y element coordinate | required|


## Mark Nx Task

```xml
<?xml version="1.0" encoding="utf-8"?>
<details name="058">
    <task answer="2" arbiter="CountArbiter" bgcolor="#FFF2F2F2" class="MarkNxTask" main="058.png"
        marker="058m.png" number="4" property="E2C" sound="058.m4a" sx="138" sy="132">
        <field name="A" x="86" y="459" />
        <field name="B" x="278" y="463" />
        <field name="C" x="494" y="464" />
        <field name="D" x="689" y="463" />
    </task>
</details>
```

Task specific elements:

| Element | Description | Comments |
|:---------:|:-----------:|:--------:|
| number    | Number of fields to select| required|
| answer    | Correct number of marked items | required|
|field      | Field to select | required **number attribute value** times|
|name       | Field name | required|
| x         | X element coordinate | required|
| y         | Y element coordinate | required|

## Move N Task

```xml
<?xml version="1.0" encoding="utf-8"?>
<details name="003">
    <task answer="A C" arbiter="ExternalArbiter" bgcolor="#FFF2F2F2" class="MoveNTask" main="003.png"
        number="4" pattern="003s%s.m4a" place="003p.png" property="M2C" sound="003.m4a">
        <field name="A" mask="003mA.png" x="89" y="116" />
        <field name="B" mask="003mB.png" x="119" y="270" />
        <field name="C" mask="003mC.png" x="89" y="390" />
        <field name="D" mask="003mD.png" x="107" y="584" />
    </task>
</details>
```

Task specific elements:

| Element | Description | Comments |
|:---------:|:-----------:|:--------:|
| number    | Number of fields to select| required|
| answer    | Correct answers names| required|
|place      | Drop field for elements | required|
|field      | Field to select | required **number attribute value** times|
|name       | Field name | required|
| x         | X element coordinate | required|
| y         | Y element coordinate | required|
|mask       | Element image mask | required|

## Move Task

```xml
<?xml version="1.0" encoding="utf-8"?>
<details name="054">
    <task answer="4" arbiter="NumberArbiter" bgcolor="#FFF2F2F2" class="MoveTask" item="054i.png"
        main="054.png" number="8" place="054p.png" property="E2CU" sound="054.m4a">
        <field name="A" px="141" py="136" x="94" y="260" />
        <field name="B" px="233" py="135" x="185" y="260" />
        <field name="C" px="332" py="136" x="286" y="260" />
        <field name="D" px="449" py="135" x="400" y="260" />
    </task>
</details>

```

Task specific elements:

| Element | Description | Comments |
|:---------:|:-----------:|:--------:|
| number    | Number of fields to select| required|
| answer    | Correct answers names| required|
|place      | Drop field for elements | required|
|field      | Field to select | required **number attribute value** times|
|name       | Field name | required|
| px      | end X coordinate of hand mask| required|
| py      | end Y coordinate of hand mask| required|
| x      | begin X coordinate of hand mask| required|
| y      | begin Y coordinate of hand mask| required|
|mask       | Element image mask | required|

## Clock Task

```xml
<?xml version="1.0" encoding="utf-8"?>
<details name="005">
    <task answer="17:25" arbiter="ClockArbiter" bgcolor="#FFF2F2F2" class="ClockTask" main="005.png"
        property="E2CU" sound="005.m4a" sx="538" sy="395">
        <field name="A" mask="005h.png" px="15" py="180" x="19" y="164" />
        <field name="B" mask="005m.png" px="15" py="60" x="19" y="220" />
    </task>
</details>
```

Task specific elements:

| Element | Description | Comments |
|:---------:|:-----------:|:--------:|
| answer    | Task answer in HH:MM format| required|
| sx        | x coordinate of clock hands | required|
| sy        | y coordinate of clock hands | required|
| field     | Clock hands | required 2 times|
| name      | clock hand name| required|
| mask      | clock hand mask| required|
| px      | end X coordinate of hand mask| required|
| py      | end Y coordinate of hand mask| required|
| x      | begin X coordinate of hand mask| required|
| y      | begin Y coordinate of hand mask| required|

## Select Task

```xml
<?xml version="1.0" encoding="utf-8"?>
<details name="001">
    <task answer="C" arbiter="SelectArbiter" bgcolor="#FFF2F2F2" class="SelectTask" main="001.png"
        marker="001m.png" number="4" property="E2C" sound="001.m4a">
        <field name="A" x="942" y="22" />
        <field name="B" x="942" y="211" />
        <field name="C" x="942" y="398" />
        <field name="D" x="942" y="587" />
    </task>
</details>
```

Task specific elements:

| Element | Description | Comments |
|:---------:|:-----------:|:--------:|
| number    | Number of fields to select| required|
| answer    | Correct answer name| required|
|field      | Field to select | required **number attribute value** times|
|name       | Field name | required|
| x         | X element coordinate | required|
| y         | Y element coordinate | required|

## Multi Select Task

```xml
<?xml version="1.0" encoding="utf-8"?>
<details name="066">
    <task answer="A C" arbiter="SelectArbiter" bgcolor="#FFF2F2F2" class="MultiSelectTask" main="066.png"
        marker="066m.png" number="4" property="E2C" sound="066.m4a">
        <field name="A" x="22" y="24" />
        <field name="B" x="22" y="214" />
        <field name="C" x="22" y="406" />
        <field name="D" x="22" y="596" />
    </task>
</details>
```

Task specific elements:

| Element | Description | Comments |
|:---------:|:-----------:|:--------:|
| number    | Number of fields to select| required|
| answer    | Correct answers names| required|
|field      | Field to select | required **number attribute value** times|
|name       | Field name | required|
| x         | X element coordinate | required|
| y         | Y element coordinate | required|




# task.xml

```xml
<?xml version="1.0" encoding="utf-8"?>
<task name="001" area="m" class="SelectTask" family="001" range="1">
    <irt a="1.2340" b="-1.124" c="0.10" />
</task>
```

| Element | Description | Comments |
|:---------:|:-----------:|:--------:|
|name     | Name of task corresponding to name in details.xml| required|
|area     | Test area for this task| required|
|family   | Task family, groups tasks| required|
|range    | mark range depends on **1** or **2** or **O** | required|
|a, b ,c aX, bX, cX| IRT mark modifiers. X depends on range 1-range value| required|
