# Task Creation

This document describes how to create particular tasks in tasks suites.

## Described tasks types

1. [Counting Task Group](#counting-task-group)
1. [CountingTask](#counting-task)
1. [CountingSummaryTask](#counting-summary-task)
1. [FourFieldsTaskGroup](#four-fields-task-group)
1. [FourFieldsBoardTask](#four-fields-board-task)
1. [Grid Task Group](#grid-task-group)
1. [GridTask](#grid-task)
1. [TutorialTask](#tutorial-task)
1. [Tutorial End Task](#tutorial-end-task)

## Arbiters

| Element | Description | xml entry |
|:-------:|:-----------:|:---------:|
| CountingArbiter | Arbiter for counting task. | tpr.cordova.CountingArbiter |
| FourFieldsBoardArbiter | Arbiter for four fields task | tpr.cordova.FourFieldsBoardArbiter |
| GridArbiter | Arbiter for grid task |tpr.cordova.GridArbiter|


## Counting Task Group

Must occur before [CountingTask](#counting-task). Works only with [CountingArbiter](#counting-arbiter).

Example:

```xml
<task class="tpr.cordova.counting.CountingTaskGroup" arbiter="tpr.cordova.CountingArbiter" />
```

## Counting Task

Example:

```xml
  <task class="tpr.cordova.counting.CountingTask" main="001.png" property="S"
    drawing_area_left="450" drawing_area_top="453" drawing_area_right="1080" drawing_area_bottom="712">
        <item image="001a.png" correct="true" quantity="1" width="80" height="80" />
  </task>
```


Task specific elements:

| Element | Description | Comments |
|:---------:|:-----------:|:--------:|
| drawing_area_left      | left border of drawing area | required |
| drawing_area_top     | top border of drawing area | required |
| drawing_area_right | right border of drawing area | required |
| drawing_area_bottom | bottom border of drawing area | required |
| item | displayed item on board | required at least one |
| image | image of item | required |
| correct | is this item counted as correct | required |
| quantity | number of displayed elements of this item | required if "range" not exists |
| range | range of displayed elements of this item | required if "quantity" not exists |
| width | width of displayed item | optional |
| height | height of displayed item | optional |


## Counting Summary Task


Displays as many as [CountingTask](#counting-task) occurrence mark boards since last [CountingSummaryTask](#counting-summary-task)

Example:

```xml
  <task  class="tpr.cordova.counting.CountingSummaryTask" main="empty.png" property="S" showCorrect="false" markRange="1,5" />
```

Task specific elements:

| Element | Description | Comments |
|:---------:|:-----------:|:--------:|
| showCorrect | determines if task should show correct answers at end | optional |
| markRange | count range of items in previous  [CountingTask](#counting-task) sequence | optional |

## Four Fields Task Group

Must occur before [FourFieldsBoardTask](#four-fields-board-task). Works only with [FourFieldsBoardArbiter](#four-fields-board-arbiter).

Example:

```xml
<task class="tpr.cordova.fourfields.FourFieldsTaskGroup"  arbiter="tpr.cordova.FourFieldsBoardArbiter"/>
```

## Four Fields Board Task




Example:

```xml
  <task class="tpr.cordova.fourfields.FourFieldsBoardTask" main="002.png" property="S"
     location="UL" config="2,3" ULSound="002c.m4a" URSound="002c.m4a"  LLSound="002r.m4a" LRSound="002r.m4a">
        <faces>
            <face image="face1.png" id="1" />
            <face image="face2.png" id="2" />
            <face image="face3.png" id="3" />
            <face image="face4.png" id="4" />
        </faces>
        <button mode="positive" location="755,535,913,680" hover="002_mark_small.png" />
        <button mode="negative" location="917,535,1077,680" hover="002_mark_small.png" />
    </task>
```

Task specific elements:

| Element | Description | Comments |
|:---------:|:-----------:|:--------:|
| location | start location of task | required |
| config | configuration of task | required |
| ULSound | sound of upper left element| required |
| URSound | sound of upper right element| required |
| LLSound | sound of lower left element| required |
| LRSound | sound of lower left element| required |
| faces | container of faces | required |
| face | face displayed to user | required at least once |
| id | face id | required |
| button | answer button | required for positive and negative |
| mode | mode of button. Possible values positive, negative | required |
| location | location of button | required|
| hover | hover image of button | required |


## Grid Task Group

Must occur before [GridTask](#grid-task). Works only with [GridArbiter](#grid-arbiter).

Example:

```xml
  <task class="tpr.cordova.grid.GridTaskGroup" arbiter="tpr.cordova.GridArbiter" />
```



## GridTask


Example:

```xml
    <task class="tpr.cordova.grid.GridTask" main="003.png" property="S" gridPosition="268,84,870,653"
      gridColumns="6" girdRows="6" mode="tutorial" showCorrectAnswer="true" infoSound="003_2.m4a" >
          <entry  image="003_2.png" imageShadow="003_3.png" timeout="1000" />
          <entry  image="003_2.png" imageShadow="003_3.png" timeout="1000" />
    </task>
```

| Element | Description | Comments |
|:---------:|:-----------:|:--------:|
| gridPosition | position of grid in order: left,top,right,bottom | required |
| gridColumns | number of columns in grid | required |
| girdRows | number of rows in column | required |
| mode | mode of task. Possible values: normal,tutorial | required |
| showCorrectAnswer | should show correct answers at end of task | optional |
| infoSound | info sound for task | optional |
| entry | image displayed for user | required at least once |
| image | entry image | required |
| imageShadow | entry shadow | required |
| timeout | display time | required |


## Tutorial Task
Example:

```xml
  <task class="tpr.TutorialTask" main="002.png" sound="002_t1.m4a" property="S" refreshTime="100">
        <item image="002_mark_big.png" location="90,69,725,375" displayStart="14000" displayTime="19000" zindex="0" />
    </task>
```

| Element | Description | Comments |
|:---------:|:-----------:|:--------:|
| item | item to display | required at least once |  
| image | item image | required |
| location | location of item  | required|
| displayStart | start display | required |
| displayTime | display time | required |
| zindex | z index of item. 0 - backgroud, >0 - foreground | required

## Tutorial End Task
Example:

```xml
  <task class="tpr.TutorialEndTask" main="confirm.png"  property="" loopBegin="0" >
    <button mode="positive" location="409,303,565,448" hover="002_mark_small.png" />
    <button mode="negative" location="570,303,725,448" hover="002_mark_small.png" />
  </task>
```
| Element | Description | Comments |
|:---------:|:-----------:|:--------:|
|loopBegin | jumps to given task index | required |
| button | answer button | required for positive and negative |
| mode | mode of button. Possible values positive, negative | required |
| location | location of button | required|
| hover | hover image of button | required |












-
