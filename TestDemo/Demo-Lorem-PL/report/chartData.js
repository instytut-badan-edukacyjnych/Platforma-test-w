var n = 3, m = 4, data = pv.range(n).map(function() {
    return pv.range(m).map(function() {
        return Math.random() + .1;
      });
  });

var a = [];
for(var i=0;i<18;++i) a.push( Math.random() );
for(var i=0;i<5;++i) a[i] = a[i]*5-3;
for(var i=5;i<12;++i) a[i] = a[i]*20-10;
for(var i=12;i<18;++i) a[i] = a[i]*150-75;

var bullets = [
  {
    title: "Cecha #1",
    subtitle: "pkt",
    ranges: [20+a[0], 25+a[1], 30+a[2]],
    measures: [23+a[3]],
    markers: [26+a[4]]
  },
  {
    title: "Cecha #2",
    subtitle: "#",
    ranges: [45+a[5], 75+a[6], 90+a[7]],
    measures: [30+a[8], 80+a[9], 85+a[10]],
    markers: [25+a[11]]
  },
  {
    title: "Cecha #3",
    subtitle: "N",
    ranges: [350+a[12], 500+a[13], 600+a[14]],
    measures: [320+a[15], 380+a[16]],
    markers: [550+a[17]]
  },
];

