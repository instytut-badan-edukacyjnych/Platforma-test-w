
      function wrapRealProvider(rp) {
        if(!rp) return null;
        return new function() {
          this.getResults = function() {
            return JSON.parse(rp.getResults());
          };
          this.getExaminee = function(id) {
            return JSON.parse(rp.getExaminee(id));
          };
          this.getInstitution = function(id) {
            return JSON.parse(rp.getInstitution(id));
          };
        }
      }

      // this.resultsProvider is optionally provided by the application, when displaying a real report
      // this.sampleResultsProvider is provided by the sampleData.js file and contains random data
      var rp = wrapRealProvider(this.resultsProvider || this.sampleResultsProvider);

