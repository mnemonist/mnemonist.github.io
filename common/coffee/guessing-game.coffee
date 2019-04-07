root = exports ? this

#################################################################################
## Dummy outer Function to keep jQuery calls out of the main object            ##
#################################################################################
root.outerPlayGame = ->
	$( "#number-choice" ).toggle( "blind", {}, 1 );
	$( "#the-guess" ).toggle( "blind", {}, 1 );
	$( "#the-verdict" ).toggle( "blind", {}, 1 );
	root.GameObject.playGame()
	$( "#number-choice" ).toggle( "blind", {}, 300 );
	$( "#the-guess" ).delay(500).toggle( "blind", {}, 300);
	$( "#the-verdict" ).delay(1000).toggle( "blind", {}, 300 );

#################################################################################
## GameObject wraps all the important things, don't see the point in creating   #
## a Singleton Pattern here                                                     #
## (http://coffeescriptcookbook.com/chapters/design_patterns/singleton)         #
#################################################################################
root.GameObject =
	totalGames: 0
	humanWins: 0
	machineWins: 0
	valueOne: 1
	valueTwo: 10000

	inEnglish: (flag, value) ->
		boolToWord =
			choice:
				true: "first"
				false: "second"
			guess:
				true: "bigger"
				false: "smaller"
			verdict:
				true: "win"
				false: "loose"
		boolToWord[flag]["#{value}"]

	playGame: () ->
		if (@valueOne == @valueTwo)
			alert "Number and Number two should have different values"
			return

		@totalGames = @totalGames + 1

		valueOneRevealed = true
		value = @valueOne

		## Choose randomly the first number or the second number
		if (Math.random() > 0.5)
			valueOneRevealed = false
			value = @valueTwo

		## Guess if the chosen number is bigger or smaller
		guessAsBigger = false
		arbitraryNumber = Math.random() * 10000
		if (value > arbitraryNumber) then (guessAsBigger = true)

		## Check if the guess is correct
		chosenNumberIsBigger = false
		if (valueOneRevealed is true)
			chosenNumberIsBigger = (value > @valueTwo)
		else
			chosenNumberIsBigger = (value > @valueOne)

		win = false
		if (((guessAsBigger is true) and (chosenNumberIsBigger is true)) or
				((guessAsBigger is false) and (chosenNumberIsBigger is false)))
					@machineWins = @machineWins + 1
					win = true

		if (((guessAsBigger is true) and (chosenNumberIsBigger is false)) or
				((guessAsBigger is false) and (chosenNumberIsBigger is true)))
					@humanWins = @humanWins + 1

		## Update user about the result of the last game
		messageBoxString1 = "My choice is to look at the " +
            "#{@inEnglish	'choice', valueOneRevealed} number"
		messageBoxString2 = "My guess is that the #{@inEnglish 'choice', valueOneRevealed} " +
            "number is #{@inEnglish 'guess', guessAsBigger}"
		messageBoxString3 = "I #{@inEnglish 'verdict', win}!"

		document.getElementById('the-choice-p').innerHTML = messageBoxString1
		document.getElementById('the-guess-p').innerHTML = messageBoxString2
		document.getElementById('the-verdict-p').innerHTML = messageBoxString3
		document.getElementById('the-stats-p').innerHTML = "#{@humanWins}                 " +
            "VS.                 #{@machineWins}"

#################################################################################
## Hook the above functions jQuery UI and the DOM elements
#################################################################################

$ ->
	$("#slider-one").slider
		value: 1
		min: 1
		max: 10000
		slide: ( event, ui ) ->
			$("#amount-one").val(ui.value)
			GameObject.valueOne = ui.value
	$("#amount-one").val( $("#slider-one").slider("value"))

	$("#slider-two").slider
		value: 10000
		min: 1
		max: 10000
		slide: ( event, ui ) ->
			$("#amount-two").val(ui.value)
			GameObject.valueTwo = ui.value
	$("#amount-two").val( $("#slider-two").slider("value"))

	$( "button", ".play-game" ).button()
	$( "button", ".play-game" ).click(outerPlayGame)

