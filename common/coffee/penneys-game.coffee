root = exports ? this

#################################################################################
## GameObject wraps all the important things, don't see the point in creating
## a Singleton Pattern here
## (http://coffeescriptcookbook.com/chapters/design_patterns/singleton)
#################################################################################
root.GameObject =
	totalGames: 0
	humanWins: 0
	machineWins: 0
	humanSequence: ["T", "T", "T"]
	machineSequence: ["H", "H", "H"]
	gameSequence: []
	message: "?"

	coinFlip: () ->
		if (Math.random() > 0.5)
			"H"
		else
			"T"

	chooseMachineSequence: () ->
		@machineSequence[1] = @humanSequence[0]
		@machineSequence[2] = @humanSequence[1]
		@machineSequence[0] = "H"
		@machineSequence[0] = "T" if (@humanSequence[1] is "H")

	generateRandomSequence: (array) ->
		i = 0
		while i < array.length
			array[i] = @coinFlip()
			i++

	compareSequences: (A, B) ->
		match = true
		i = 0
		while i < B.length
			if A[i] isnt B[i]
				match = false
				break
			i++
		match

	toggleHumanSequence: (id) ->
		if (@humanSequence[id] is "H")
			@humanSequence[id] = "T"
		else
			@humanSequence[id] = "H"
		@humanSequence[id]

	getMessage: () ->
		@message

	###############################################################################
	## playGame is the main function that starts the guessing game
	## for a give set of human and machine values
	###############################################################################
	playGame: () ->
		@totalGames = @totalGames + 1

		## Start with two coin flips
		@gameSequence = []
		@gameSequence.push(@coinFlip())
		@gameSequence.push(@coinFlip())

		unmatched = true
		while unmatched is true
			@gameSequence.push(@coinFlip())

			#console.log("Game sequence is #{@gameSequence}")
			gameSeqLength = @gameSequence.length
			gameSeqEndPart = @gameSequence[(gameSeqLength - 3)..(gameSeqLength - 1)]
			if @compareSequences(@humanSequence, gameSeqEndPart ) is true
				@humanWins = @humanWins + 1
				unmatched = false
				#console.log("Human wins...")
				@message = "You Win!"
				break

			if @compareSequences(@machineSequence, gameSeqEndPart ) is true
				@machineWins = @machineWins + 1
				unmatched = false
				#console.log("Machine wins...")
				@message = "I Win!"

#################################################################################
## Jquery/GameLogic Glue Layer, delegated to call Jquery objects
#################################################################################
root.outerChooseMachineSequence = ->
	root.GameObject.chooseMachineSequence()
	$('#your-seq > a').each (i, element) ->
		$(element).children('.ui-btn-inner').children('.ui-btn-text').text(root.GameObject.humanSequence[i])
	$('#my-seq > a').each (i, element) ->
		$(element).children('.ui-btn-inner').children('.ui-btn-text').text(root.GameObject.machineSequence[i])


root.outerPlayGame =
	counter: 0
	timerID: 0

	beginGame: () ->
		@counter = 0
		$('#result-your-seq > a').each (i, element) ->
			$(element).children('.ui-btn-inner').children('.ui-btn-text').text(root.GameObject.humanSequence[i])
		$('#result-my-seq > a').each (i, element) ->
			$(element).children('.ui-btn-inner').children('.ui-btn-text').text(root.GameObject.machineSequence[i])
		$("#winner").text("?")

		root.GameObject.playGame()
		for j in [0..9]
			cell = '#' + "cell-#{j}"
			$("#{cell}").text("")
		root.timerID = setInterval @animateResultStep, 750

	## For whatever reason when this function is called by SetInterval, @counter does not work.
	## => does not work too.... Scratching my head.
	animateResultStep: () ->
		#console.log("animateResultStep called with #{@counter}")
		if (root.outerPlayGame.counter is root.GameObject.gameSequence.length)
			$("#winner").text(root.GameObject.getMessage())
			clearInterval root.timerID
			return
		else
			for j in [0..8]
				leftCell = '#' + "cell-#{j}"
				rightCell = '#' + "cell-#{j+1}"
				$("#{leftCell}").text($("#{rightCell}").text())
			$('#cell-9').text(root.GameObject.gameSequence[root.outerPlayGame.counter])
			root.outerPlayGame.counter += 1

	updateStats: () ->
		$("#human-wins").text(root.GameObject.humanWins)
		$("#machine-wins").text(root.GameObject.machineWins)
		return

#################################################################################
## Hook the above functions to jQuery UI on the DOM events
#################################################################################

$ ->
	$( '#my-choice' ).bind 'pageinit', () ->
		root.outerChooseMachineSequence()
		$( '#go-to-my-choice' ).click (event) ->
			root.outerChooseMachineSequence()

	$( '#result' ).bind 'pageinit', () ->
		root.outerPlayGame.beginGame()
		$( '#go-to-your-choice' ).click (event) ->
			root.outerPlayGame.beginGame()

	$( '#your-choice' ).bind 'pageinit', () ->
		$('#usr-seq > a').each (i, element) ->
			$(element).children('.ui-btn-inner').children('.ui-btn-text').text(root.GameObject.humanSequence[i])

	$( '#stats' ).bind 'pageinit', () ->
		root.outerPlayGame.updateStats()
		$( '#go-to-stats' ).click (event) ->
			root.outerPlayGame.updateStats()

	$( '#usr-seq > a' ).click (event) =>
		id = $(event.currentTarget).attr 'id'
		newVal = root.GameObject.toggleHumanSequence(id)
		$(event.currentTarget).children('.ui-btn-inner').children('.ui-btn-text').text(newVal)


