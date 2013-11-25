// ---------------------------
// Legal
// ---------------------------
// CastleRater Control
// Castle Rock Software, LLC
// by Mark Wagner
// http://blogs.crsw.com/mark
//
// Version: 1.1
//
// Copyright 2005 Castle Rock Software, LLC
//
// No warranty express or implied.
//

// ---------------------------
// Documentation
// ---------------------------
// The CastleRater javascript has been written so the CastleRater javascript
// control can be used with or without the Castle.Rater .NET control.

// ---------------------------
// Constructor
// ---------------------------
// CastleRater class constructor
// This class constructor should be treated as private.  This CastleRater object should be
// instantiated using the CastleRater.Create method.
function CastleRater(id, imageOnUrl, imageOffUrl, imageHoverUrl, maxValue, value)
{
	// Assign default values for undefined or null parameters
	if( value == undefined || value == null )
		value = 0;
	
	if( maxValue == undefined || maxValue == null )
		maxValue = 5;
	
	//
	// Initialize default values for instance properties
	//
	
	// ID contains the client-side unique id of this control
	this.ID = id;
	
	// MaxValue: Get or Set the maximum value for this control
	this.MaxValue = maxValue;
	
	// Value: Get or Set the value for this control.
	this.Value = value;
	
	// Added by Michael Yudelson
	this.HoverValue = value;
	
	// ImageOn: name of the image used to represent the selected value
	this.ImageOn = imageOnUrl;
	
	// ImageOff: name of the image used to represent the available values to select
	this.ImageOff = imageOffUrl;
	
	// ImageHover: name of the image used to provide user feedback when hovering over the control
	this.ImageHover = imageHoverUrl;
		
	// AutoLock: (if true) will disable this control once a value has been selected (clicked).
	this.AutoLock = false;

	// IsEnabled returns a boolean value indicating if the user may use this control.  ReadOnly.
	this.IsEnabled = true;

	// IsInitialized: returns a boolean value indicating if this rater control has been initialized.  ReadOnly.
	this.IsInitialized = false;
	
	// Images: an Array containing an entry for each image in this control.
	this.Images = new Array(maxValue);
	
	// Container: provides a reference to the html object acting as the outer most container 
	// for this control.  This property is assigned a value during the Render method.
	this.Container = null;
	
	this.ValueControl = null;
	this.ValueControlID = this.ID + "_Value";
	
	// Cache (pre-load) images
	new Image().src = this.ImageOn;
	new Image().src = this.ImageOff;
	new Image().src = this.ImageHover;
}

// ---------------------------
// Class Properties
// ---------------------------

CastleRater.Item2s = Array();

// ---------------------------
// Class Functions
// ---------------------------

// CastleRater.Create - class function
// id:
// imageOnUrl:
// imageOffUrl:
// imageOverUrl:
// maxValue:
// value:
CastleRater.Create = function(id, imageOnUrl, imageOffUrl, imageHoverUrl, maxValue, value)
{
	// Create new CastleRater object
	var obj = new CastleRater(id, imageOnUrl, imageOffUrl, imageHoverUrl, maxValue, value);
	
	// Add the new Rater object to the Class items array
	CastleRater.Item2s[obj.ID] = obj;
	
	// Return new Rater object
	return obj;
}

// CastleRater.CreateNETControl - class function
// id:
// imageOnUrl:
// imageOffUrl:
// imageOverUrl:
// maxValue:
// value:
CastleRater.CreateNETControl = function(id, imageOnUrl, imageOffUrl, imageHoverUrl, maxValue, value)
{
	// Create an instance of the Rater object. 
	var rater = CastleRater.Create(id, imageOnUrl, imageOffUrl, imageHoverUrl, maxValue, value);
	
	// Initialize javascript object for use with the .NET control.
	rater.Initialize();
	
	// Return new Rater object
	return rater;
}

// CastleRater.CreateJSControl - class function
// id:
// imageOnUrl:
// imageOffUrl:
// imageOverUrl:
// maxValue:
// value:
CastleRater.CreateJSControl = function(id, imageOnUrl, imageOffUrl, imageHoverUrl, maxValue, value)
{
	// Create an instance of the Rater object. 	
	var rater = CastleRater.Create(id, imageOnUrl, imageOffUrl, imageHoverUrl, maxValue, value);
	
	// Initialize javascript object for use with the .NET control.
	rater.Render();
	
	// Return new Rater object
	return rater;
}

// Class function 
CastleRater.InitializeRater = function(rater)
{
	for(i = 1; i <= rater.MaxValue; i++)
	{
		var imgID = rater.ID + "_img" + i;
		var img = document.getElementById(imgID);
		rater.Images[i] = img;
	}
	
	// Find the html tag representing this control and retain a reference to it
	rater.Container = document.getElementById(rater.ID);
	// this.Container.style.cursor = "hand"; // This is only valid for IE.  Causes no problem for NS or Firefox.
	
	// Save the new value in the hidden input control
	var myItem2s = document.getElementsByName(rater.ValueControlID);
	rater.ValueControl = myItem2s[0];
	rater.ValueControl.value = rater.Value;
	
	rater.IsInitialized = true;
}

// Class Function
// source: image object calling this function.  Usually "this".
// raterID: ID of the CastleRater object.
CastleRater.Refresh = function(source, raterID)
{
	var rater = CastleRater.Item2s[raterID];
	var value = rater.Value;
	
	for(var i = 1; i <= rater.MaxValue; i++)
	{
		if( value >= i )
			rater.Images[i].src = rater.ImageOn;
		else
			rater.Images[i].src = rater.ImageOff;
	}
}

// CastleRater.ShowHover - class function
// source: image object calling this function.  Usually "this".
// raterID: ID of the CastleRater object.
CastleRater.Hover = function(source, raterID)
{
	var rater = CastleRater.Item2s[raterID];
	if( !rater.IsEnabled ) return;
	
	var value = source.getAttribute("value");
	rater.HoverValue = value;
	
	for(var i = 1; i <= rater.MaxValue; i++)
	{
		if( value >= i )
			rater.Images[i].src = rater.ImageHover;
		else
			rater.Images[i].src = rater.ImageOff;
	}
}

// CastleRater.Click - class function
// source: image object calling this function.  Usually "this".
// raterID: ID of the CastleRater object.
CastleRater.Click = function(source, raterID)
{
	var rater = CastleRater.Item2s[raterID];
	if( !rater.IsEnabled ) 
		return;
	
	// Get new value.
	rater.Value = source.getAttribute("value");
	
	// Update the hidden input control with the new value.
	rater.ValueControl.value = rater.Value;
	
	// Repaint the control.
	CastleRater.Refresh(source, raterID);
	
	if( rater.AutoLock )
		rater.Enable(false);
}

// ---------------------------
// Instance Functions
// ---------------------------

// Instance function 
CastleRater.prototype.Initialize = function()
{
	CastleRater.InitializeRater(this);
}

// Instance function
CastleRater.prototype.Enable = function(enable)
{
	this.Container.disabled = !enable;
	this.IsEnabled = enable;
}

// Instance function
CastleRater.prototype.Visible = function(visible)
{
	if( visible )
		this.Container.style.visibility = "Visible";
	else
		this.Container.style.visibility = "Hidden";
}

// Instance function
CastleRater.prototype.Render = function()
{
	document.write("<input id=" + this.ValueControlID + " type=hidden>");
	document.write("<span id=\"" + this.ID + "\">");
	
	for(var i = 1; i <= this.MaxValue; i++)
	{
		// Use the correct image in order correctly reflect the current value for this Rater control.
		if( this.Value >= i )
			imageName = this.ImageOn;
		else
			imageName = this.ImageOff;	
		
		// Important Note: IE does NOT support dynamically setting html tag EVENT attributes
		// using javascript.  However IE does support dynamically creating the html tags events 
		// as long as the html tag is rendered to the document using the javascript 
		// document.write method.
		// These examples do NOT work in IE:
		//		this.Images[i].setAttribute("onclick", "CastleRater.Click(this, \"" + this.ID + "\");");
		// or
		//		this.Images[i].onclick = "CastleRater.Click(this, \"" + this.ID + "\");";
		
		// Get the next available image index number
		var index = document.images.length;
		
		// Render the new img tag.  Also, any tag events must be defined here to work in IE.
		var onclick = " onclick=\"CastleRater.Click(this, '" + this.ID + "');\" ";
		var onmouseover = " onmouseover=\"CastleRater.Hover(this, '" + this.ID + "');\" ";
		var onmouseout = " onmouseout=\"CastleRater.Refresh(this, '" + this.ID + "');\" ";
		var imgID = this.ID + "_img" + i;
		document.write("<img id=\"" + imgID + "\" " + onclick + onmouseover + onmouseout + ">");
		
		// Add a reference to the newly created img tag to our images collection
		this.Images[i] = document.images[index];
		
		// Set the image properties.  Note: IE requires html tag events to be defined 
		// when the html tag is rendered, so they are defined with the <img> tag.
		this.Images[i].src = imageName;
		this.Images[i].setAttribute("imageSrc", imageName);
		this.Images[i].setAttribute("imageHover", this.ImageHover);
		this.Images[i].setAttribute("value", i);
		this.Images[i].border = 0;
	}
	
	document.write("</span>");
	
	if( this.IsInitialized == false )
		this.Initialize();
}
