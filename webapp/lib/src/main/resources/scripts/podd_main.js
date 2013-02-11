/*
 * Copyright (c) 2009 - 2010. School of Information Technology and Electrical
 * Engineering, The University of Queensland.  This software is being developed
 * for the "Phenomics Ontoogy Driven Data Management Project (PODD)" project.
 * PODD is a National e-Research Architecture Taskforce (NeAT) project
 * co-funded by ANDS and ARCS.
 *
 * PODD is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * PODD is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with PODD.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * We will display an alert if the user is accessing PODD with IE6 or below */
function displayBrowserAlert() {
    var browserVersion = getInternetExplorerVersion();
    if (navigator.appName == "Microsoft Internet Explorer" && browserVersion <= 7) {
      alert("PODD does not support Internet Explorer version 7 or below ! \n\n" +
            "It is recommended that you use another browser or upgrade your copy of Internet Explorer.");
    }
}

/*
 * What version of internet explorer are we dealing with ? */
function getInternetExplorerVersion() {
    var rv = -1; // Return value assumes failure.
    if (navigator.appName == 'Microsoft Internet Explorer') {
        var ua = navigator.userAgent;
        var re = new RegExp("MSIE ([0-9]{1,}[\.0-9]{0,})");
        if (re.exec(ua) != null)
            rv = parseFloat(RegExp.$1);
    }
    return rv;
}

/*
 * Remove the filter from the form
 */
function removeFilter() {
	var form = document.forms.removeFilterForm;
	form.submit();
}

/*
 * Write the string shortened to the given length, including only whole words. 
 * 
 * TODO: When the user clicks on the abstract, show the entire string
 * 
 */
function writeAbstractWholeWords(inputStr, length) {
	var space = " ";
	// if string is less than the given length the whole string should be written
	var endChar = length - 1;
	// if string is less than 200 character long write the given string
	if (inputStr.length > length) {
		// get the closest whole word to the given length
		while (inputStr.charAt(endChar) != space) {
			endChar--;
		}
	}
	document.write(inputStr.substring(0, endChar));
}


/*
 * Validate administration - create new user form */
function createUserFormValidator(){
	var validUserId = true;
	// only the create user page allows us to set the user name
	document.getElementById('errorUserName').innerHTML = '';
	var userName = document.getElementById('userName');
	if(isEmptyField(userName) || !isMinimumLength(userName, 6) || !isValidUserId(userName)){
		validUserId = false;
		document.getElementById('errorUserName').innerHTML = 'User name must be at least 6 characters';
	}
	var validInput = validateUserInfo();
	var validPassword = validateUserPassword();
	return validUserId && validInput && validPassword;
}
/*
 * Validate the users data fields (except password) */
function validateUserInfo(){

	var validInput = true;

	// reset the error message fields to empty string
	document.getElementById('errorEmail').innerHTML = '';
	document.getElementById('errorFirstName').innerHTML = '';
	document.getElementById('errorLastName').innerHTML = '';
	document.getElementById('errorOrganisation').innerHTML = '';
	document.getElementById('errorPhone').innerHTML = '';
	document.getElementById('errorAddress').innerHTML = '';

	// fields requiring validation
	var email = document.getElementById('email');
	var firstName = document.getElementById('firstName');
	var lastName = document.getElementById('lastName');
	var organisation = document.getElementById('organisation');
	var phone = document.getElementById('phone');
	var address = document.getElementById('address');

	// Check each input in the order that it appears in the form
	if(isEmptyField(email) || !isValidEmail(email)){
		validInput = false;
		document.getElementById('errorEmail').innerHTML = 'Please enter a valid e-mail address';
	}

	if(isEmptyField(firstName)){
		validInput = false;
		document.getElementById('errorFirstName').innerHTML = 'Required field';
	}

	if(isEmptyField(lastName)){
		validInput = false;
		document.getElementById('errorLastName').innerHTML = 'Required field';
	}

	if(isEmptyField(organisation)){
		validInput = false;
		document.getElementById('errorOrganisation').innerHTML = 'Required field';
	}

	if(isEmptyField(phone)){
		validInput = false;
		document.getElementById('errorPhone').innerHTML = 'Required field';
	}

	if(isEmptyField(address)){
		validInput = false;
		document.getElementById('errorAddress').innerHTML = 'Required field';
	}

	return validInput;
}

/* 
 * Validate the users password */
function validateUserPassword(){
	
	var validInput = true;
	
	// reset the error message fields to empty string
	document.getElementById('errorPassword').innerHTML = '';
	document.getElementById('errorConfirmPassword').innerHTML = '';

	// fields requiring validation
	var password = document.getElementById('password');
	var confirmPassword = document.getElementById('confirmPassword');
	
	// Check each input in the order that it appears in the form
	if(isEmptyField(password) || !isMinimumLength(password, 6) || !isNonSpaceCharater(password)){
		validInput = false;
		document.getElementById('errorPassword').innerHTML = 'Password must be at least 6 characters with no spaces';
	}
	if(confirmPassword.value != password.value){
		validInput = false;
		document.getElementById('errorConfirmPassword').innerHTML = 'Passwords must match';
	}
	return validInput;
}

function valiadateDescription() {
    var valid = true;
    var address = document.getElementById('object_description');
    // Check each input in the order that it appears in the form
    if(isEmptyField(address)){
        document.getElementById('errorDescription').innerHTML = 'Description can not be empty.';
        valid = false;
    }
    return valid;
}


/* ----- functions to validate form fields ----- */

/* check the element's string is empty */
function isEmptyField(elem){
	if(elem.value.length == 0){
		return true;
	}
	return false;
}

/* check the element's string has at least the minimum length */
function isMinimumLength(elem, length){
	if(elem.value.length >= length){
		return true;
	}
	return false;
}

/* check the element's string contains only numbers and letters */
function isAlphanumeric(elem){
	var alphaExp = /^[0-9a-zA-Z]+$/;
	if(elem.value.match(alphaExp)){
		return true;
	}	
	return false;
}

/* check the element's string contains a valid email address */
function isValidEmail(elem){
	var emailExp = /^[\w\-\.\+]+\@[a-zA-Z0-9\.\-]+\.[a-zA-z0-9]{2,4}$/;
	if(elem.value.match(emailExp)){
		return true;
	}
	return false;
}

/* check the element's string contains a valid user name */
function isValidUserId(elem){
	var userIdExp = /[\w-_\.]+/;
	if(elem.value.match(userIdExp)){
		return true;
	}
	return false;
}

/* check the element's string is between the minimum and maximum length */
function lengthRestriction(elem, min, max){
	var userInput = elem.value;
	if(userInput.length >= min && userInput.length <= max){
		return true;
	}
	return false;
}

/* check the element's string is all numbers */
function isNumeric(elem){
	var numericExpression = /^[0-9]+$/;
	if(elem.value.match(numericExpression)){
		return true;
	}
	return false;
}

/* check the element's string is all letters */
function isAlphabet(elem){
	var alphaExp = /^[a-zA-Z]+$/;
	if(elem.value.match(alphaExp)){
		return true;
	}
	return false;
}

function isNonSpaceCharater(elem){
	var alphaExp = /^[^\s]+$/;
	if(elem.value.match(alphaExp)){
		return true;
	}
	return false;
}

var ContentHeight = 200;
var TimeToSlide = 250.0;

var openItem = '';

function openLogin(){
	if(openItem == "AAFLOGINFORM"){
		setTimeout("animate(" + new Date().getTime() + "," + TimeToSlide 
				+ ",'AAFLOGINFORM','AAFLOGINLOGO')", 33);
		openItem = "AAFLOGINLOGO"
	} else {
		setTimeout("animate(" + new Date().getTime() + "," + TimeToSlide 
				+ ",'AAFLOGINLOGO','AAFLOGINFORM')", 33);
		openItem = "AAFLOGINFORM";
	}
}

function animate(lastTick, timeLeft, closingId, openingId)
{  
  var curTick = new Date().getTime();
  var elapsedTicks = curTick - lastTick;
 
  var opening = (openingId == '') ? null : document.getElementById(openingId);
  var closing = (closingId == '') ? null : document.getElementById(closingId);
 
  if(timeLeft <= elapsedTicks)
  {
    if(opening != null)
      opening.style.height = ContentHeight + 'px';
   
    if(closing != null)
    {
      closing.style.display = 'none';
      closing.style.height = '0px';
    }
    return;
  }
 
  timeLeft -= elapsedTicks;
  var newClosedHeight = Math.round((timeLeft/TimeToSlide) * ContentHeight);

  if(opening != null)
  {
    if(opening.style.display != 'block')
      opening.style.display = 'block';
    opening.style.height = (ContentHeight - newClosedHeight) + 'px';
  }
 
  if(closing != null)
    closing.style.height = newClosedHeight + 'px';

  setTimeout("animate(" + curTick + "," + timeLeft + ",'"
      + closingId + "','" + openingId + "')", 33);
}
