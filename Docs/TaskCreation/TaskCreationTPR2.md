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
<task class="tpr.actionscript.counting.CountingTaskGroup" arbiter="tpr.actionscript.CountingArbiter"
 abstract="true" />
```

| Element | Description | Comments |
|:---------:|:-----------:|:--------:|
|abstract  | determines if test is abstract| required|

## Counting Task

Example:

```xml
  <task class="tpr.actionscript.counting.CountingTask" main="ramka.png" property="S" drawing_area_left="10"
    drawing_area_top="10" drawing_area_right="1080" drawing_area_bottom="720">
        <item image="bb.png" correct="true" quantity="3" width="100" height="100" />
        <item image="yb.png" correct="false" quantity="3" width="100" height="100" />
        <item image="bs.png" correct="false" quantity="5" width="100" height="100" />
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
  <task class="tpr.actionscript.counting.CountingSummaryTask" main="ramka.png" property=""
    showCorrect="true" markRange="3,9" />
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
<task class="tpr.actionscript.fourfields.FourFieldsTaskGroup"  arbiter="tpr.actionscript.FourFieldsBoardArbiter"
  abstract="true"/>
```
| Element | Description | Comments |
|:---------:|:-----------:|:--------:|
|abstract  | determines if test is abstract| required|


## Four Fields Board Task




Example:

```xml
  <task class="tpr.actionscript.fourfields.FourFieldsBoardTask" main="plansza6sama.png" property="S"
      coordinatesLL="300,342,560,603" coordinatesUL="300,63,560,327" coordinatesUR="561,63,820,327"
      coordinatesLR="561,342,820,603"  coordinatesText="350,620,800,710" fontSize="48.0" isTutorial="true" >
        <entry text="173" location="UL" question="Nieparzysta?" isCorrect="true" timeout="3000" />
        <entry text="728" location="UR" question="Nieparzysta?" isCorrect="false" timeout="3000" />
        <entry text="161" location="LR" question="Mniejsza od 500?" isCorrect="true" timeout="3000" />
        <entry text="607" location="LL" question="Większa od 500?" isCorrect="true" timeout="3000" />

        <button mode="positive" coordinates="900,560,1070,720"  normal="yes.png"
           pressed="yesDown.png" goodAnswer="yesDownOk.png" wrongAnswer="yesDownNo.png" />
        <button mode="negative" coordinates="50,560,220,720" normal="no.png"
          pressed="noDown.png" goodAnswer="noDownOk.png" wrongAnswer="noDownNo.png" />
    </task>
```

Task specific elements:

| Element | Description | Comments |
|:---------:|:-----------:|:--------:|
| location | start location of task | required |
| config | configuration of task | required |
| coordinatesUL | coordinates of upper left element| required |
| coordinatesUR | coordinates of upper right element| required |
| coordinatesLL | coordinates of lower left element| required |
| coordinatesLR | coordinates of lower left element| required |
| coordinatesText | coordinates of text | required|
| fontSize | font size | optional |
| entry | displayed entry | required  at least once |
| location | location of entry | required |
| text | text to display  | required  or image|
| image | image to display | required or text |
| isCorrect | determines if entry is correct| required |
| timeout | timeout | required|
| button | answer button | required for positive and negative |
| mode | mode of button. Possible values positive, negative | required |
| coordinates | coordinates of button | required|
| normal, pressed,goodAnswer, wrongAnswer |states of button | required |


## Grid Task Group

Must occur before [GridTask](#grid-task). Works only with [GridArbiter](#grid-arbiter).

Example:

```xml
  <task class="tpr.actionscript.grid.GridTaskGroup" arbiter="tpr.actionscript.GridArbiter"  abstract="true"/>
```
| Element | Description | Comments |
|:---------:|:-----------:|:--------:|
|abstract  | determines if test is abstract| required|


## GridTask


Example:

```xml
    <task class="tpr.actionscript.grid.GridTask" main="ramka.png" property="" gridPosition="245,50,870,660"
       gridColumns="10" girdRows="10" mode="tutorial" showCorrectAnswer="true">
        <entry image="fyy.png" imageShadow="fyh.png" timeout="1000" />
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




## Auto Tutorial End Task
Example:

```xml
  <task class="tpr.AutoTutorialEndTask" main="wlasciwa_info.jpg" property=""
     failureImage="powtorz_zadanie.jpg" loopBegin="5" />
```
| Element | Description | Comments |
|:---------:|:-----------:|:--------:|
|loopBegin | jumps to given task index | required |
| failureImage | image displayed on tutorial part failure | required |













-
