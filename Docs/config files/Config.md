#Config

##config.xml

Test configuration

```xml
<?xml version="1.0" encoding="utf-8"?>
<config>
    <suite pilot="yes" />
    <tasks change="TaskChangeOpaque" click="yes" finish="E00" haptic="yes" manager="KttManager"
        password="yes" />
</config>
```


| Element | Attribute | Description | Comments | Possible values | Status |
|:-------:|:---------:|:-----------:|:--------:|:---------------:|:------:|
| tasks   |  -----    | Tasks description | required once| ----- | -----  |
| tasks   | change    | Change method between tasks| legacy support, don't use it | string | deprecated |
| tasks   | click     | sound feedback on click| optional | yes, no | ------|
| tasks   | finish    | End task for task suite| legacy support, don't use it | string | deprecated |
| tasks   | haptic    | Haptic feedback on click | optional | yes, no | -----|
| tasks   | manager   | Manager for tests | required | KttManager, CatManager, CbtManager | ----- |
| tasks   | password  | Determines if password is required to login |  legacy support, don't use it | yes,no | deprecated |
| suite   | pilot     | Determines if task suite is in pilot mode | optional | yes, no | ----- |


##Managers:

1. KttManager
1. CatManager
1. CbtManager

You can create own manager by extending **BaseManager** class and using above listed
managers as examples.

After creating new manager you should create new class implementing **ManagerFactory**
interface and put it in **pl.edu.ibe.loremipsum.manager.managers**.

**createManager** method in this interface should create new instance of your manager.
