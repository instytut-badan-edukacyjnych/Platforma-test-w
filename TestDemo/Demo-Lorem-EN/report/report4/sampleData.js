      // "sent" by application: flat list of conducted researches
      var ____localResults = [
        // {
        //   'id': '123', 'inst': 'sp1', 'dept': 'kubusie', 'examinee':'ididid', 'date':'2000-12-31T23:59:59+02:00',
        //   'areas' : { 'cecha0': { 'score': 33.4 }, 'cecha1': { 'score': 55.4 }, ... }
	    // }
      ];

      // randomizer, until application will not present real data
      function randomDate(start, end) {
        return new Date(start.getTime() + Math.random() * (end.getTime() - start.getTime()));
      }
      var ____exid = 0;
      $.each({'i0':['rabbits','cowboys'],'i1':['rabbits','butterflies','cowboys']}, function(inst,depts) {
        $.each(depts, function(i,dept) {
          var n = 15 + Math.random()*90;
          for(var it=0;it<n;++it) {
            ____localResults.push({
              'id': ++____exid,
              'examinee': Math.floor( 1 + Math.random()*5 ),
              'areas': {
                'm': { 'score': Math.random()*5-2.5 },
                'p': { 'score': Math.random()*5-2.5 },
                'c': { 'score': Math.random()*5-2.5 },
              },
              'inst': inst,
              'date': randomDate(new Date(2014,1,1), new Date(2014,10,31)).toISOString(),
              'dept': dept,
            });
          }
        });
      });

      var ____localInsts = {
        'i0': { 'name': 'school #1', 'postal': '99-222', 'city': 'Nowheretown' },
        'i1': { 'name': 'school #2', 'postal': '12-321', 'city': 'Somewheretown'},
      };

      /* var ____localDepts = {     - currently DEPT has no key
        'd0': 'puchatki',
        'd1': 'kubusie',
        'd2': 'rodzynki',
      }; */

      var ____localExmn = {
        '1': { 'name': 'Cajek', 'surname': 'Bodyr', 'birthday': '24-03-2008' },
        '2': { 'name': 'Wadia', 'surname': 'Romea', 'birthday': '24-03-2008' },
        '3': { 'name': 'Jordek', 'surname': 'Goral', 'birthday': '24-03-2008' },
        '4': { 'name': 'Poga', 'surname': 'Malicka', 'birthday': '24-03-2008' },
        '5': { 'name': 'Rokal', 'surname': 'Hrywiec', 'birthday': '24-03-2008' },
      };

      var sampleResultsProvider = {
        'getResults': function() { return JSON.stringify(____localResults);},
        'getExaminee': function(id) { return JSON.stringify(____localExmn[id]);},
        'getInstitution': function(id) { return JSON.stringify(____localInsts[id]);},
      };
