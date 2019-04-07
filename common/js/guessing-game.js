(function() {
  var root;
  root = typeof exports !== "undefined" && exports !== null ? exports : this;
  root.outerPlayGame = function() {
    $("#number-choice").toggle("blind", {}, 1);
    $("#the-guess").toggle("blind", {}, 1);
    $("#the-verdict").toggle("blind", {}, 1);
    root.GameObject.playGame();
    $("#number-choice").toggle("blind", {}, 300);
    $("#the-guess").delay(500).toggle("blind", {}, 300);
    return $("#the-verdict").delay(1000).toggle("blind", {}, 300);
  };
  root.GameObject = {
    totalGames: 0,
    humanWins: 0,
    machineWins: 0,
    valueOne: 1,
    valueTwo: 10000,
    inEnglish: function(flag, value) {
      var boolToWord;
      boolToWord = {
        choice: {
          "true": "first",
          "false": "second"
        },
        guess: {
          "true": "bigger",
          "false": "smaller"
        },
        verdict: {
          "true": "win",
          "false": "loose"
        }
      };
      return boolToWord[flag]["" + value];
    },
    playGame: function() {
      var arbitraryNumber, chosenNumberIsBigger, guessAsBigger, messageBoxString1, messageBoxString2, messageBoxString3, value, valueOneRevealed, win;
      if (this.valueOne === this.valueTwo) {
        alert("Number and Number two should have different values");
        return;
      }
      this.totalGames = this.totalGames + 1;
      valueOneRevealed = true;
      value = this.valueOne;
      if (Math.random() > 0.5) {
        valueOneRevealed = false;
        value = this.valueTwo;
      }
      guessAsBigger = false;
      arbitraryNumber = Math.random() * 10000;
      if (value > arbitraryNumber) {
        guessAsBigger = true;
      }
      chosenNumberIsBigger = false;
      if (valueOneRevealed === true) {
        chosenNumberIsBigger = value > this.valueTwo;
      } else {
        chosenNumberIsBigger = value > this.valueOne;
      }
      win = false;
      if (((guessAsBigger === true) && (chosenNumberIsBigger === true)) || ((guessAsBigger === false) && (chosenNumberIsBigger === false))) {
        this.machineWins = this.machineWins + 1;
        win = true;
      }
      if (((guessAsBigger === true) && (chosenNumberIsBigger === false)) || ((guessAsBigger === false) && (chosenNumberIsBigger === true))) {
        this.humanWins = this.humanWins + 1;
      }
      messageBoxString1 = "My choice is to look at the " + ("" + (this.inEnglish('choice', valueOneRevealed)) + " number");
      messageBoxString2 = ("My guess is that the " + (this.inEnglish('choice', valueOneRevealed)) + " ") + ("number is " + (this.inEnglish('guess', guessAsBigger)));
      messageBoxString3 = "I " + (this.inEnglish('verdict', win)) + "!";
      document.getElementById('the-choice-p').innerHTML = messageBoxString1;
      document.getElementById('the-guess-p').innerHTML = messageBoxString2;
      document.getElementById('the-verdict-p').innerHTML = messageBoxString3;
      return document.getElementById('the-stats-p').innerHTML = ("" + this.humanWins + "                 ") + ("VS.                 " + this.machineWins);
    }
  };
  $(function() {
    $("#slider-one").slider({
      value: 1,
      min: 1,
      max: 10000,
      slide: function(event, ui) {
        $("#amount-one").val(ui.value);
        return GameObject.valueOne = ui.value;
      }
    });
    $("#amount-one").val($("#slider-one").slider("value"));
    $("#slider-two").slider({
      value: 10000,
      min: 1,
      max: 10000,
      slide: function(event, ui) {
        $("#amount-two").val(ui.value);
        return GameObject.valueTwo = ui.value;
      }
    });
    $("#amount-two").val($("#slider-two").slider("value"));
    $("button", ".play-game").button();
    return $("button", ".play-game").click(outerPlayGame);
  });
}).call(this);
