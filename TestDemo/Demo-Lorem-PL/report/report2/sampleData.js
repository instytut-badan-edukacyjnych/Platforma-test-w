      // "przysylane" przez aplikacje: plaska lista wynikow badan
      var ____localResults = [
        // {
        //   'id': '123', 'inst': 'sp1', 'dept': 'kubusie', 'examinee':'ididid', 'date':'2000-12-31T23:59:59+02:00',
        //   'areas' : { 'cecha0': { 'score': 33.4 }, 'cecha1': { 'score': 55.4 }, ... }
	    // }
      ];

      // randomizer, dopoki aplikacja nie bedzie wystawiac faktycznych danych
      var ____exid = 0;
      $.each({'i0':['puchatki','kubusie'],'i1':['puchatki','kubusie','rodzynki']}, function(inst,depts) {
        $.each(depts, function(i,dept) {
          var n = Math.random()*10;
          for(var it=0;it<n;++it)
            ____localResults.push({
              'id': ++____exid,
              'examinee': Math.floor( 1 + Math.random()*5 ),
              'areas': {
                'm': { 'score': Math.random()*5-2.5 },
                'p': { 'score': Math.random()*5-2.5 },
                'c': { 'score': Math.random()*5-2.5 },
              },
              'inst': inst,
              'date': new Date(2014,1,2+____exid,Math.random()*24,Math.random()*60,Math.random()*60).toISOString(),
              'dept': dept,
            });
        });
      });

      var ____localInsts = {
        'i0': { 'name': 'szkoła pierwsza', 'postal': '00-222', 'city': 'Warszawa' },
        'i1': { 'name': 'szkoła druga', 'postal': '81-170', 'city': 'Gdynia'},
      };

      /* var ____localDepts = {     - obecnie DEPT nie ma klucza
        'd0': 'puchatki',
        'd1': 'kubusie',
        'd2': 'rodzynki',
      }; */

      var ____localExmn = {
        '1': { 'name': 'Cajek', 'surname': 'Bodyr', 'birthday': '24-03-2008' },
        '2': { 'name': 'Wadia', 'surname': 'Rómań', 'birthday': '24-03-2008' },
        '3': { 'name': 'Jordek', 'surname': 'Góral', 'birthday': '24-03-2008' },
        '4': { 'name': 'Poga', 'surname': 'Malicka', 'birthday': '24-03-2008' },
        '5': { 'name': 'Rokal', 'surname': 'Hrywiec', 'birthday': '24-03-2008' },
      };

      var sampleResultsProvider = {
        'getResults': function() { return JSON.stringify(____localResults);},
        'getExaminee': function(id) { return JSON.stringify(____localExmn[id]);},
        'getInstitution': function(id) { return JSON.stringify(____localInsts[id]);},
      };
