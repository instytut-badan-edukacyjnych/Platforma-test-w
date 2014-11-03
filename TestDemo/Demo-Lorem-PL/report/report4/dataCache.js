      function hashKeys(h) {
        return $.map(h,function(v,k){return k;});
      }

      function kvpToHash(kvps) {
        var res = {};
        $.each(kvps, function(idx,v){res[v[0]] = v[1];});
        return res;
      }

      function rebuildCache(rp){
        var cache1 = {
          '':{ '':{ 'areas': { }, 'avgs': { }, } }
        };
        var cache2 = {
        };

        $.each(rp.getResults(), function(idx, exam){
          var inst = exam['inst'] || '';
          var dept = exam['dept'] || '';
          var exid = exam['id'];
          var prid = exam['examinee'];
          var res = exam['areas'];

          cache2[exid] = exam;

          var cTotTot = cache1[''][''];

          var cTotDept = cache1[''][dept];
          if(!cTotDept) cTotDept = cache1[''][dept] = { 'areas': { }, 'avgs': { }, };

          var cInst = cache1[inst];
          if(!cInst) cInst = cache1[inst] = { '':{ 'areas': { }, 'avgs': { }, } };

          var cInstTot = cInst[''];

          var cInstDept = cInst[dept];
          if(!cInstDept) cInstDept = cInst[dept] = { 'areas': { }, 'avgs': { }, };

          bins = [ cTotTot, cTotDept, cInstTot, cInstDept ];

          $.each(res, function(area, val){
            $.each(bins, function(idx, bin){
              var arrTTD = bin['areas'][area];
              if(!arrTTD) arrTTD = bin['areas'][area] = [];
              arrTTD.push(val);
            });
          });
        });

        var cache3 = kvpToHash( $.map($.unique($.map(cache2, function(v,k){return v['examinee'];})), function(v,k){return [[v,rp.getExaminee(v)]];}) );
        var cache4 = kvpToHash( $.map(Object.keys(cache1).filter(function(v){return v!='';}), function(v,k){return [[v,rp.getInstitution(v)]];}) );
        var cache5 = Object.keys(cache1['']).filter(function(v){return v!='';}); // - obecnie DEPT nie ma klucza ani danych
        
        return {
          'examinees': cache3,
          'exams': cache2,
          'insts': cache4,
          'depts': cache5,
          'inst>dept': cache1,
        };
      };


      var __localResults = rebuildCache(rp);


      function getPersonalResults() {
        //return __personalResults;
        var arr = $.map(getLocalExams(""), function(v,k){return v;}).sort(function(a,b){
          return b.id - a.id;
        });
        if(!arr[0]) {
          return {
            'id': null,
            'examinee': null,
          };
        };
        return {
          'id': arr[0].id,
          'examinee': arr[0].examinee,
        };
      }

      function getLocalExaminees() {
        return __localResults['examinees'];
      }

      function getLocalDepts() {
        return __localResults['depts'];
      }

      function getLocalInsts() {
        return __localResults['insts']
      }

      function getLocalGroups() {
        return $.map(__localResults['inst>dept'],function(deps,inst){
          return $.map(hashKeys(deps),function(d){ return [[d,inst]];});
        });
      }

      function getLocalResults(inst,dep) {
        var l1 = __localResults['inst>dept'][inst||''];
        if(!l1) return [];
        return l1[dep||''] || [];
      }

      function getLocalExams(examinee) {
        var l1 = __localResults['exams'];
        if(l1 && examinee) {
          l1 = $.map(l1, function(v,k){return v;});
          l1 = l1.filter(function(it){return it['examinee'] == examinee;});
          l1.sort(function(a,b){
            return a.id - b.id;
          });
        }
        return l1 || [];
      }

