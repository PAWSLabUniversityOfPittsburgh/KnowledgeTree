// CAPXOUS JavaScript Library (Version 1.1.8 r20060913173439)
var isKHTML=navigator.appVersion.match(/Konqueror|Safari|KHTML/);
var isOpera=navigator.userAgent.indexOf('Opera')>-1;
var isIE=!isOpera&&navigator.userAgent.indexOf('MSIE')>1;
var isMoz=!isOpera&&!isKHTML&&navigator.userAgent.indexOf('Mozilla/5.')==0;
var CAPXOUS=new Object();

CAPXOUS=
{
	getHashCode:function(o)
	{
		var sum=0;
		for(i=0;i<o.length;i++)
		{
			sum+=o.charCodeAt(i);
		};
		var base='ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/';
		var h=base.substr(sum&63,1);
		while(sum>63)
		{
			sum>>=6;
			h=base.substr(sum&63,1)+h;
		};
		return h;
	},
	
	isRegistered:function(o)
	{
		return true;//***MVY
	},
	
	getWatermark:function()
	{
		return"";//***MVY
	},
	
	removeBraces:function(text)
	{
		return text.substring(text.indexOf('{')+1,text.lastIndexOf('}'));
	},
	
	MD1:function(text)
	{
		return CAPXOUS.removeBraces(text.toString()).replace(new RegExp("[\\s\.{}();\\\"\\'\\\\/]","g"),'').length;
	},
	
	MD2:function(text)
	{
		return CAPXOUS.removeBraces(text.toString()).replace(new RegExp("[\\s\.{}();\\\"\\'\\\\/CAPXOUS]","ig"),'').length;
	},
	
	updateStyle:function(e)
	{
		while(e=e.parentNode)
		{
			if(e.style)
			{
				if(e.style.overflow=='hidden')
					e.style.overflow='visible';
				if(e.style.tableLayout=='fixed')
					e.style.tableLayout='auto';
			}
		}
	}
};

CAPXOUS.AutoComplete=Class.create();


Object.extend(CAPXOUS.AutoComplete,
{
	removeWatermark:function(name,key)
	{
		var cls=CAPXOUS.AutoComplete;
		cls.LicenseOwner=name+' AutoComplete';
		cls.LicenseKey=key;
	},
	
	style:{wait:'CAPXOUS_AutoComplete_waiting'},
	
	findPopup:function(v)
	{
		var e=Event.element(v);
		while(e&&e.parentNode&&!e.object)
			e=e.parentNode;
		if(e==null) return null;
		
		return e.parentNode?e:null;
	},
	
	isSelectable:function(e)
	{
		return(e.nodeType==1)&&(e.getAttribute('onselect'));
	},
	
	findSelectable:function(v,p)
	{
		var e=Event.element(v);
		while(e.parentNode&&(e!=p)&&(!CAPXOUS.AutoComplete.isSelectable(e)))
			e=e.parentNode;
		return(e.parentNode&&(e!=p))?e:null;
	},
	
	process:function(e,o)
	{
		var ajaxHref=e.getAttribute('ajaxHref');
		if(!Element.hasClassName(e,'usual'))
		{
			var url;
			if(ajaxHref)url=e.getAttribute('ajaxHref');
			else url=e.getAttribute('href');
			o.request(url);
		}
	},
	
	click:function(v)
	{
		var cls=CAPXOUS.AutoComplete;
		var e=Event.element(v);
		var p=cls.findPopup(v);
		if(p)
		{
			var s=cls.findSelectable(v,p);
			if(s)
			{
				p.object.i=s.getAttribute(cls.index);
				p.object.select();
			}
			else
			{
				while(e.parentNode&&(e!=p)&&(!e.tagName||e.tagName.toUpperCase()!='A'))
					e=e.parentNode;if(e.parentNode&&(e!=p))cls.process(e,p.object,v);
			}
		}
		else
		{
			cls.inst.each(function(i)
				{
					if(i.text!=e&&i.update!=e)setTimeout(i.hide.bind(i),10);
				}
			);
		}
	},
	
	mouseover:function(v)
	{
		var cls=CAPXOUS.AutoComplete;
		var p=cls.findPopup(v);
		if(p)
		{
			var s=cls.findSelectable(v,p);if(s)p.object.highlight(s.getAttribute(cls.index));
		}
	},
		
		
	init:function()
	{
		var p=document.createElement('div');
		p.className=CAPXOUS.AutoComplete.style.wait;
		var s=p.style;
		s.display='inline';s.position='absolute';s.width=s.height=s.top=s.left='0px';
		document.body.appendChild(p);
		
		if(isIE)
			CAPXOUS.selfName=self.name;
			
		setTimeout(function()
			{
				if((CAPXOUS.MD2(CAPXOUS.isRegistered)!=56)||(CAPXOUS.MD2(CAPXOUS.getHashCode)!=134)||
						(CAPXOUS.MD2(CAPXOUS.getWatermark)!=242)||(CAPXOUS.MD2(CAPXOUS.AutoComplete.prototype.initialize)!=532)||
						(CAPXOUS.MD2(CAPXOUS.MD1)!=44)||(CAPXOUS.MD2(CAPXOUS.MD2)!=45))
				{
					;//CAPXOUS['AutoComplete'].prototype.keyPress=function(){};//***MVY
				}
			},
			1986);
	},
		
	index:'index',inst:new Array(),name:'',key:'',
	
	getWindowHeight:function()
	{
		var h=0;
		if(typeof(window.innerHeight)=='number')
		{
			h=window.innerHeight;
		}
		else if(document.documentElement&&document.documentElement.clientHeight)
		{
			h=document.documentElement.clientHeight;
		}
		else if(document.body&&document.body.clientHeight)
		{
			h=document.body.clientHeight;
		};
		return parseInt(h);
	},
	
	getStyle:function(e)
	{
		if(!isKHTML&&document.defaultView&&document.defaultView.getComputedStyle)
		{
			return document.defaultView.getComputedStyle(e,null);
		}
		else if(e.currentStyle)
		{
			return e.currentStyle;
		}
		else
		{
			return e.style;
		}
	},
	
	getInt:function(s)
	{
		var i=parseInt(s);
		return isNaN(i)?0:i;
	}
});


Event.observe
(
	window,'load',CAPXOUS.AutoComplete.init);Event.observe(window,'load',
	
	function()
	{
		setTimeout(function()
		{
			if((CAPXOUS.MD1(CAPXOUS.isRegistered)!=84)||(CAPXOUS.MD1(CAPXOUS.getHashCode)!=184)||
					(CAPXOUS.MD1(CAPXOUS.getWatermark)!=357)||(CAPXOUS.MD1(CAPXOUS.AutoComplete.prototype.initialize)!=799)||
					(CAPXOUS.MD1(CAPXOUS.MD1)!=65)||(CAPXOUS.MD1(CAPXOUS.MD2)!=73)
				)
			{
				 ;//CAPXOUS['AutoComplete'].prototype.request=function(){}; //***MVY
			}
		},4290);
	});
	
	CAPXOUS.AutoComplete.prototype=
	{
		visible:false,complete:false,initialized:false,
		initialize:function(text,f,options)
		{
			text=$(text)?$(text):document.getElementsByName(text)[0];
			if((text==null)||(f==null)||(typeof f!='function'))return;
			text.setAttribute('autocomplete','off');
			this.onchange=text.onchange;text.onchange=function(){};
			this.txtBox=this.text=text;
			this.installObservers();
			this.options=options||{};
			this.options.frequency=this.options.frequency||0.4;
			this.options.minChars=this.options.minChars||1;
			this.timeout=0;this.getURL=f;
			this.buffer=document.createElement('div');
			var p=document.createElement('div');
			p.object=this;
			Element.addClassName(p,'CAPXOUS_AutoComplete');
			var ps=p.style;ps.position='absolute';
			ps.top='-999px';ps.height='auto';
			Element.hide(p);
			this.update=p;this.i=-1;
			var cls=CAPXOUS.AutoComplete;
			cls.inst.push(this);
			if(cls.inst.length==1)
			{
				Event.observe(document,'click',cls.click);
				Event.observe(document,'mouseover',cls.mouseover);
			};
//			if(!CAPXOUS.isRegistered(cls)) ***MVY
//			{
//				new Insertion.After(this.text,CAPXOUS.getWatermark());
//				CAPXOUS.updateStyle(this.text);
//			};
			
			this.cls=cls;
		},
		
		installObservers:function()
		{
			this._keydown=this.keydown.bindAsEventListener(this);
			this._request=this.request.bind(this);
			this._focus=this.focus.bind(this);
			Event.observe(this.text,'keydown',this._keydown);
			Event.observe(this.text,'keyup',this._keydown);
			Event.observe(this.text,'dblclick',this._request);
			Event.observe(this.text,'focus',this._focus);
		},
		
		page:function(name)
		{
			var s=document.getElementsByClassName(name);
			var e=s.first();
			if(e&&e.tagName&&e.tagName.toUpperCase()=='A')
				this.cls.process(e,this);
		},
		
		focus:function()
		{
			if(!this.visible)this.request();
		},
		
		keyPress:function(event)
		{
			var keyCode=event.keyCode;
			if(keyCode==38||keyCode==40)
			{
				if(this.complete)
				{
					(keyCode==38)?this.up():this.down();this.show();
				};
			};
			if(keyCode==33||keyCode==34)
			{
				if(this.complete)
					(keyCode==33)?this.page('page_up'):this.page('page_down');
			};
			if(keyCode==27)
				this.hide();
			if(keyCode==38||keyCode==40||keyCode==33||keyCode==34||keyCode==27||keyCode==13)
			{
				Event.stop(event);return;
			};
			switch(keyCode)
			{
				case 9:case 37:case 39:case 35:case 36:case 45:case 16:case 17:case 18:break;
				default:if(this.timeout!=0)clearTimeout(this.timeout);
				this.cancelRequest();
				this.timeout=setTimeout(this._request,this.options.frequency*1000);
			}
		},
		
		
		cancelRequest:function()
		{
			if(this.latestRequest)this.latestRequest.transport.abort();
		},
		
		keydown:function(event)
		{
			var keyCode=event.keyCode;if(keyCode==9)
			{
				if(event.type=='keydown'&&this.visible)
					this.select();
				return;
			};
			if(keyCode==13)
			{
				Event.stop(event);
				if(event.type=='keyup')
				{
					if(this.latestKeyCode==13&&this.latestType=='keydown')
					{
						this.visible?this.select():this.request();
					}
					else
					{
						this.request();
					}
				}
			};
			this.latestType=event.type;
			this.latestKeyCode=event.keyCode;
			if(event.type=='keydown'&&keyCode!=13)
				this.keyPress(event);
		},
		
		select:function()
		{
			if(this.getCurrentEntry())
			{
				var stat=this.getCurrentEntry().getAttribute('onselect');
				try{eval(stat);}
				catch(e){};
				this.hide();
				if(this.onchange)
				{
					setTimeout(function()
						{
							this.onchange.bind(this.text)();
						}.bind(this),100);
				}
			}
		},
		
		getCurrentEntry:function()
		{
			return this.children?this.children[this.i]:null;
		},
		
		highlight:function(i)
		{
			if(!this.complete)return;
			Element.removeClassName(this.getCurrentEntry(),'current');
			this.i=i;Element.addClassName(this.getCurrentEntry(),'current');
		},
		
		up:function()
		{
			if(this.i>-1)this.highlight(this.i-1);
		},
		
		down:function(){if(this.i<this.children.length-1)this.highlight(this.i+1);},
		
		preRequest:function(){return this.text.value.length>=this.options.minChars;},
		
		request:function(url)
		{
			if(typeof url!='string')url=false;if(this.preRequest())
			{
				if(this.getAjaxURL)this.getAjaxURL();
				if(url)this.onLoading(true);
				else this.onLoading();
				var _url=this.getURL();
				if(typeof _url=='string')
				{
					if(url)
					{
						var loc=location.protocol+'//'+location.host+location.pathname;
						if((url.charAt(0)=='?')||((url.indexOf(loc)==0)&&(url.charAt(loc.length)=='?')))
						{
							if(url.charAt(0)!='?')url=url.substr(loc.length);_url+='&'+url.substr(1);
						}
						else{_url=url;};
					};
					this.requestURL=_url=encodeURI(_url);
					this.latestRequest=new Ajax.Updater
					(
						this.buffer,_url,{method:'get',onComplete:this.onComplete.bind(this),onFailure:this.onFailure.bind(this)}
					);
				}
			}
			else{this.cancelRequest();this.stopIndicator();this.hide();}
		},
		
		onFailure:function(transport){},
		
		onLoading:function(){this.complete=false;this.i=-1;this.startIndicator();},
		
		onComplete:function(){setTimeout(this.updateContent.bind(this,arguments[0]),10);},
		
		core:function()
		{
			if(!this.initialized)
			{
				this.initialized=true;document.body.appendChild(this.update);
			};
			this.i=-1;this.children=new Array();
			if(isIE)this.fixIE();
			$A(this.update.getElementsByTagName('a')).each
			(
				function(a)
				{
					if(!Element.hasClassName(a,'usual'))
					{
						a.onclick=function(){return false;};
					};
				}
			);
			$A(this.update.getElementsByTagName('*')).each
			(
				function(c)
				{
					if(this.cls.isSelectable(c))
					{
						c.setAttribute(this.cls.index,this.children.length);
						Element.addClassName(c,'selectable');
						this.children.push(c);
					}
				}.bind(this)
			);
			this.complete=true;
			this.down();
			if(!this.visible)this.show();
			this.stopIndicator();
		},
		
		updateContent:function()
		{
			var tx=this.latestRequest.transport;
			var t=((this.latestRequest==null)||(tx==arguments[0]));
			if(t)
			{
				if(this.latestRequest.url!=this.requestURL)return;
				this.complete=true;
				try
				{
					var success=this.latestRequest.responseIsSuccess();
				}
				catch(e){return;};
				if(success)
				{
					var text=null;
					if((tx.responseXML)&&(tx.responseXML.documentElement))
					{
						var docE=tx.responseXML.documentElement;
						if(docE.nodeName=='string')
						{
							if(docE.text)
							{
								text=docE.text;
							}
							else if(docE.textContent)
							{
								text=docE.textContent;
							}
							else if(docE.firstChild.nodeValue)
							{
								text=docE.firstChild.nodeValue;
							}
						}
					};
					if(text==null)text=this.buffer.innerHTML;
					this.update.innerHTML=text;
				}
				else
				{
					this.update.innerHTML='<h1 align="center">'+tx.status+' '+(tx.statusText?tx.statusText:'')+'</h1>';
				};
				this.buffer.innerHTML='';
			};
			this.core();
		},
		
		offset:function()
		{
			var o=0;if(isMoz||isKHTML||(isIE&&(document.compatMode!='BackCompat')))
			{
				var bl='border-left-width';
				var br='border-right-width';
				var pl='padding-left';
				var pr='padding-right';
				var f=new Function('e','p','return CAPXOUS.AutoComplete.getInt(Element.getStyle(e, p));');
				o=f(this.update,bl)+f(this.update,br)+f(this.update,pl)+f(this.update,pr);};return o;},fixIE:function(){this.update.innerHTML+="<img style='width:0px;height:0px;clear:both' align='right'/>";},fixIEOverlapping:function(){var f;if(!(f=this.iefix)){f=document.createElement('iframe');f.src='javascript:false;';var fs=f.style;fs.filter="progid:DXImageTransform.Microsoft.Alpha(opacity = 0)";fs.position='absolute';fs.margin=fs.padding='0px';Element.hide(f);document.body.appendChild(f);this.iefix=f;};self.name=CAPXOUS.selfName;Position.clone(this.update,f);f.style.zIndex=1;this.update.style.zIndex=2;Element.show(f);},show:function(status){var pos=Position.cumulativeOffset(this.text);var tt=pos[1];var th=this.text.offsetHeight;var tl=pos[0];var tw=this.text.offsetWidth;var wh=this.cls.getWindowHeight();Element.setStyle(this.update,{top:'-999px',left:'-999px',width:tw+'px',height:'auto'});Element.show(this.update);var ph=this.update.offsetHeight;Element.hide(this.update);var pt=tt+th;tw=tw-this.offset();Element.setStyle(this.update,{top:pt+'px',left:tl+'px',width:tw+'px',height:'auto'});if(isIE&&this.update.filters.length==0)this.update.style.filter="filter: progid:DXImageTransform.Microsoft.DropShadow(OffX=2, OffY=2, Color='#c0c0c0', Positive='true') progid:DXImageTransform.Microsoft.Fade(duration=0.25,overlap=1.0)";if(isIE&&!this.visible){this.update.filters[1].apply();this.update.filters[1].play();};Element.show(this.update);if(isIE)this.fixIEOverlapping();this.visible=true;},hide:function(){if(this.visible){Element.hide(this.update);if(isIE)Element.hide(this.iefix);this.visible=false;}},startIndicator:function(){Element.addClassName(this.text,this.cls.style.wait);if(this.options.indicator)Element.show(this.options.indicator);},stopIndicator:function(){Element.removeClassName(this.text,this.cls.style.wait);if(this.options.indicator)Element.hide(this.options.indicator);}};if(typeof AjaxPro!='undefined'){Object.extend(CAPXOUS.AutoComplete.prototype,{isAjaxPro:false,forAJAXPro:function(text,b){if(this.ajaxRequest==b){this.update.innerHTML=text.value;this.core();}},getAjaxURL:function(){if(!this.isAjaxPro){var source=this.getURL.toString();source=CAPXOUS.removeBraces(source);source=source.replace('CAPXOUS','this.forAJAXPro.bind(this), null, function() { this.doStateChange() }, null, null, function(a, b) { if (a == 0) this.ajaxRequest = b; }.bind(this)');this.getURL=new Function(source).bind(this);this.isAjaxPro=true;}}});};
	
		








	
	
	
	
	
	
	