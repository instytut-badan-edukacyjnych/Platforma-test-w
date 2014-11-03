var results = rp.getResults();
var total = results.length;

var inst_count = {};
var inst_dept = {};
var dept_count = {};
var dept_inst = {};

$.each( results, function(idx, res) {
  var i = rp.getInstitution( res.inst ).name;
  var d = res.dept;
  inst_count[i] = (inst_count[i] || 0) + 1;
  dept_count[d] = (dept_count[d] || 0) + 1;

  var id = inst_dept[i] || {};
  id[d] = (id[d] || 0) + 1;
  inst_dept[i] = id;

  var di = dept_inst[d] || {};
  di[i] = (di[i] || 0) + 1;
  dept_inst[d] = di;
});


var demodata1 = [];
$.each(inst_count, function(key, val) {
  var depts = [];
  var tmp = 0;
  $.each(inst_dept[key]||[], function(kk, vv) {
    tmp += vv;
    depts.push(tmp);
  });
  demodata1.push( {
    "title": key,
    "subtitle": "liczba",
    "ranges": [0,val,total],
    "markers": [val],
    "measures": depts
  });
});
$.each(dept_count, function(key, val) {
  var insts = [];
  var tmp = 0;
  $.each(dept_inst[key]||[], function(kk, vv) {
    tmp += vv;
    insts.push(tmp);
  });
  demodata1.push( {
    "title": key,
    "subtitle": "liczba",
    "ranges": [0,val,total],
    "markers": [val],
    "measures": insts
  });
});

var demodata2 = [];
$.each(inst_count, function(key, val) {
  demodata2.push( {
    "title": key,
    "value": val
  });
});
$.each(dept_count, function(key, val) {
  demodata2.push( {
    "title": key,
    "value": val
  });
});


function buildInstrs(instmap) {
  var allInst = Object.keys(instmap);
  var withoutLast = allInst.splice(0, allInst.length-1)
  var lastInst = allInst[allInst.length-1];
  var inst_str = withoutLast.join("\", \"");
  if(withoutLast.length > 0) inst_str = "\"" + inst_str + "\" i ";
  if(lastInst) inst_str += "\"" + lastInst + "\"";
  return inst_str || '';
};

