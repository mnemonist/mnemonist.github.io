(function() {
  var root;
  var __bind = function(fn, me){ return function(){ return fn.apply(me, arguments); }; };
  root = typeof exports !== "undefined" && exports !== null ? exports : this;
  root.GameObject = {
    totalGames: 0,
    humanWins: 0,
    machineWins: 0,
    humanSequence: ["T", "T", "T"],
    machineSequence: ["H", "H", "H"],
    gameSequence: [],
    message: "?",
    coinFlip: function() {
      if (Math.random() > 0.5) {
        return "H";
      } else {
        return "T";
      }
    },
    chooseMachineSequence: function() {
      this.machineSequence[1] = this.humanSequence[0];
      this.machineSequence[2] = this.humanSequence[1];
      this.machineSequence[0] = "H";
      if (this.humanSequence[1] === "H") {
        return this.machineSequence[0] = "T";
      }
    },
    generateRandomSequence: function(array) {
      var i, _results;
      i = 0;
      _results = [];
      while (i < array.length) {
        array[i] = this.coinFlip();
        _results.push(i++);
      }
      return _results;
    },
    compareSequences: function(A, B) {
      var i, match;
      match = true;
      i = 0;
      while (i < B.length) {
        if (A[i] !== B[i]) {
          match = false;
          break;
        }
        i++;
      }
      return match;
    },
    toggleHumanSequence: function(id) {
      if (this.humanSequence[id] === "H") {
        this.humanSequence[id] = "T";
      } else {
        this.humanSequence[id] = "H";
      }
      return this.humanSequence[id];
    },
    getMessage: function() {
      return this.message;
    },
    playGame: function() {
      var gameSeqEndPart, gameSeqLength, unmatched, _results;
      this.totalGames = this.totalGames + 1;
      this.gameSequence = [];
      this.gameSequence.push(this.coinFlip());
      this.gameSequence.push(this.coinFlip());
      unmatched = true;
      _results = [];
      while (unmatched === true) {
        this.gameSequence.push(this.coinFlip());
        gameSeqLength = this.gameSequence.length;
        gameSeqEndPart = this.gameSequence.slice(gameSeqLength - 3, (gameSeqLength - 1 + 1) || 9e9);
        if (this.compareSequences(this.humanSequence, gameSeqEndPart) === true) {
          this.humanWins = this.humanWins + 1;
          unmatched = false;
          this.message = "You Win!";
          break;
        }
        _results.push(this.compareSequences(this.machineSequence, gameSeqEndPart) === true ? (this.machineWins = this.machineWins + 1, unmatched = false, this.message = "I Win!") : void 0);
      }
      return _results;
    }
  };
  root.outerChooseMachineSequence = function() {
    root.GameObject.chooseMachineSequence();
    $('#your-seq > a').each(function(i, element) {
      return $(element).children('.ui-btn-inner').children('.ui-btn-text').text(root.GameObject.humanSequence[i]);
    });
    return $('#my-seq > a').each(function(i, element) {
      return $(element).children('.ui-btn-inner').children('.ui-btn-text').text(root.GameObject.machineSequence[i]);
    });
  };
  root.outerPlayGame = {
    counter: 0,
    timerID: 0,
    beginGame: function() {
      var cell, j;
      this.counter = 0;
      $('#result-your-seq > a').each(function(i, element) {
        return $(element).children('.ui-btn-inner').children('.ui-btn-text').text(root.GameObject.humanSequence[i]);
      });
      $('#result-my-seq > a').each(function(i, element) {
        return $(element).children('.ui-btn-inner').children('.ui-btn-text').text(root.GameObject.machineSequence[i]);
      });
      $("#winner").text("?");
      root.GameObject.playGame();
      for (j = 0; j <= 9; j++) {
        cell = '#' + ("cell-" + j);
        $("" + cell).text("");
      }
      return root.timerID = setInterval(this.animateResultStep, 750);
    },
    animateResultStep: function() {
      var j, leftCell, rightCell;
      if (root.outerPlayGame.counter === root.GameObject.gameSequence.length) {
        $("#winner").text(root.GameObject.getMessage());
        clearInterval(root.timerID);
      } else {
        for (j = 0; j <= 8; j++) {
          leftCell = '#' + ("cell-" + j);
          rightCell = '#' + ("cell-" + (j + 1));
          $("" + leftCell).text($("" + rightCell).text());
        }
        $('#cell-9').text(root.GameObject.gameSequence[root.outerPlayGame.counter]);
        return root.outerPlayGame.counter += 1;
      }
    },
    updateStats: function() {
      $("#human-wins").text(root.GameObject.humanWins);
      $("#machine-wins").text(root.GameObject.machineWins);
    }
  };
  $(function() {
    $('#my-choice').bind('pageinit', function() {
      root.outerChooseMachineSequence();
      return $('#go-to-my-choice').click(function(event) {
        return root.outerChooseMachineSequence();
      });
    });
    $('#result').bind('pageinit', function() {
      root.outerPlayGame.beginGame();
      return $('#go-to-your-choice').click(function(event) {
        return root.outerPlayGame.beginGame();
      });
    });
    $('#your-choice').bind('pageinit', function() {
      return $('#usr-seq > a').each(function(i, element) {
        return $(element).children('.ui-btn-inner').children('.ui-btn-text').text(root.GameObject.humanSequence[i]);
      });
    });
    $('#stats').bind('pageinit', function() {
      root.outerPlayGame.updateStats();
      return $('#go-to-stats').click(function(event) {
        return root.outerPlayGame.updateStats();
      });
    });
    return $('#usr-seq > a').click(__bind(function(event) {
      var id, newVal;
      id = $(event.currentTarget).attr('id');
      newVal = root.GameObject.toggleHumanSequence(id);
      return $(event.currentTarget).children('.ui-btn-inner').children('.ui-btn-text').text(newVal);
    }, this));
  });
}).call(this);
