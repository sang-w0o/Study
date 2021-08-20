function nextRandomInteger(limit){
	return Math.round(Math.random() * limit);
}

var alphabet = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ';
function randomAlphabet(){
	return alphabet.charAt(nextRandomInteger(25));
}

function randomSpeed(maxSpeed){
	return Math.random() * maxSpeed - Math.random() * maxSpeed;
}

var canvasWidth = 700;
var canvasHeight = 400;

function MovingText(){
	this.x = nextRandomInteger(canvasWidth);
	this.y = nextRandomInteger(canvasHeight);
	this.vx = randomSpeed(10);
	this.vy = randomSpeed(10);
	
	this.body = document.createElement('h1');
	this.body.innerHTML = randomAlphabet();
	this.body.style.position = 'absolute';
	document.body.appendChild(this.body);
}

MovingText.prototype.move = function(){
	if(this.x < 0 || this.x > canvasWidth) { this.vx *= -1;}
	if(this.y < 0 || this.y > canvasHeight) { this.vy *= -1;}
	
	this.x += this.vx;
	this.y += this.vy;
	
	this.body.style.left = this.x + 'px';
	this.body.style.top = this.y + 'px';
}

window.onload = function(){
	var movingTexts = new Array();
	for(var i = 0; i < 100; i++){
		movingTexts.push(new MovingText());
	}
	
	setInterval(function(){
		for(var i in movingTexts){
			movingTexts[i].move();
		}
	}, 1000 / 30);
}