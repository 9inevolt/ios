;(function($, window, document, undefined) {
  // RegExp that matches opening and closing browser-stripped tags.
  // $1 = slash, $2 = tag name, $3 = attributes
  var matchTag = /<(\/?)(html|head|body|title|base|meta)(\s+[^>]*)?>/ig;
  // Unique id prefix for selecting placeholder elements.
  var prefix = 'hd' + +new Date;
  // A node under which a temporary DOM tree can be constructed.
  var parent;
 
  $.htmlDoc = function(html) {
    // A collection of "intended" elements that can't be rendered cross-browser
    // with .innerHTML, for which placeholders must be swapped.
    var elems = $();
    // Input HTML string, parsed to include placeholder DIVs. Replace HTML,
    // HEAD, BODY tags with DIV placeholders.
    var htmlParsed = html.replace(matchTag, function(tag, slash, name, attrs) {
      // Temporary object in which to hold attributes.
      var obj = {};
      // If this is an opening tag...
      if ( !slash ) {
        // Add an element of this name into the collection of elements. Note
        // that if a string of attributes is added at this point, it fails.
        elems = elems.add('<' + name + '/>');
        // If the original tag had attributes, create a temporary div with
        // those attributes. Then, copy each attribute from the temporary div
        // over to the temporary object.
        if ( attrs ) {
          $.each($('<div' + attrs + '/>')[0].attributes, function(i, attr) {
            obj[attr.name] = attr.value;
          });
        }
        // Set the attributes of the intended object based on the attributes
        // copied in the previous step.
        elems.eq(-1).attr(obj);
      }
      // A placeholder div with a unique id replaces the intended element's
      // tag in the parsed HTML string.
      return '<' + slash + 'div'
        + (slash ? '' : ' id="' + prefix + (elems.length - 1) + '"') + '>';
    });
 
    // If no placeholder elements were necessary, just return normal
    // jQuery-parsed HTML.
    if ( !elems.length ) {
      return $(html);
    }
    // Create parent node if it hasn't been created yet.
    if ( !parent ) {
      parent = $('<div/>');
    }
    // Create the parent node and append the parsed, place-held HTML.
    parent.html(htmlParsed);
    // Replace each placeholder element with its intended element.
    $.each(elems, function(i) {
      var elem = parent.find('#' + prefix + i).before(elems[i]);
      elems.eq(i).html(elem.contents());
      elem.remove();
    });
    // Return the topmost intended element(s), sans text nodes, while removing
    // them from the parent element with unwrap.
    return parent.children().unwrap();
  };

        var baseUrl = "http://www.destiny.gg";

	var urlParseRE = /^\s*(((([^:\/#\?]+:)?(?:(\/\/)((?:(([^:@\/#\?]+)(?:\:([^:@\/#\?]+))?)@)?(([^:\/#\?\]\[]+|\[[^\/\]@#?]+\])(?:\:([0-9]+))?))?)?)?((\/?(?:[^\/\?#]+\/+)*)([^\?#]*)))?(\?[^#]+)?)(#.*)?/;
	
	//Parse a URL into a structure that allows easy access to
	//all of the URL components by name.
	var parseUrl = function( url ) {
		// If we're passed an object, we'll assume that it is
		// a parsed url object and just return it back to the caller.
		if ( $.type( url ) === "object" ) {
			return url;
		}

		var matches = urlParseRE.exec( url || "" ) || [];

			// Create an object that allows the caller to access the sub-matches
			// by name. Note that IE returns an empty string instead of undefined,
			// like all other browsers do, so we normalize everything so its consistent
			// no matter what browser we're running on.
			return {
				href:         matches[  0 ] || "",
				hrefNoHash:   matches[  1 ] || "",
				hrefNoSearch: matches[  2 ] || "",
				domain:       matches[  3 ] || "",
				protocol:     matches[  4 ] || "",
				doubleSlash:  matches[  5 ] || "",
				authority:    matches[  6 ] || "",
				username:     matches[  8 ] || "",
				password:     matches[  9 ] || "",
				host:         matches[ 10 ] || "",
				hostname:     matches[ 11 ] || "",
				port:         matches[ 12 ] || "",
				pathname:     matches[ 13 ] || "",
				directory:    matches[ 14 ] || "",
				filename:     matches[ 15 ] || "",
				search:       matches[ 16 ] || "",
				hash:         matches[ 17 ] || ""
			};
	};
	
	//Turn relPath into an asbolute path. absPath is
	//an optional absolute path which describes what
	//relPath is relative to.
	var makePathAbsolute = function( relPath, absPath ) {
		var absStack,
			relStack,
			i, d;

		if ( relPath && relPath.charAt( 0 ) === "/" ) {
			return relPath;
		}

		relPath = relPath || "";
		absPath = absPath ? absPath.replace( /^\/|(\/[^\/]*|[^\/]+)$/g, "" ) : "";

		absStack = absPath ? absPath.split( "/" ) : [];
		relStack = relPath.split( "/" );

		for ( i = 0; i < relStack.length; i++ ) {
			d = relStack[ i ];
			switch ( d ) {
				case ".":
					break;
				case "..":
					if ( absStack.length ) {
						absStack.pop();
					}
					break;
				default:
					absStack.push( d );
					break;
			}
		}
		return "/" + absStack.join( "/" );
	};
	
	//Returns true if both urls have the same domain.
	var isSameDomain = function( absUrl1, absUrl2 ) {
		return parseUrl( absUrl1 ).domain === path.parseUrl( absUrl2 ).domain;
	};
	
	//Returns true for any relative variant.
	var isRelativeUrl = function( url ) {
		// All relative Url variants have one thing in common, no protocol.
		return parseUrl( url ).protocol === "";
	};

	//Returns true for an absolute url.
	var isAbsoluteUrl = function( url ) {
		return parseUrl( url ).protocol !== "";
	};
	
	//Turn the specified realtive URL into an absolute one. This function
	//can handle all relative variants (protocol, site, document, query, fragment).
	var makeUrlAbsolute = function( relUrl, absUrl ) {
		if ( !isRelativeUrl( relUrl ) ) {
			return relUrl;
		}

		if ( absUrl === undefined ) {
			absUrl = this.documentBase;
		}

		var relObj = parseUrl( relUrl ),
			absObj = parseUrl( absUrl ),
			protocol = relObj.protocol || absObj.protocol,
			doubleSlash = relObj.protocol ? relObj.doubleSlash : ( relObj.doubleSlash || absObj.doubleSlash ),
			authority = relObj.authority || absObj.authority,
			hasPath = relObj.pathname !== "",
			pathname = makePathAbsolute( relObj.pathname || absObj.filename, absObj.pathname ),
			search = relObj.search || ( !hasPath && absObj.search ) || "",
			hash = relObj.hash;

		return protocol + doubleSlash + authority + pathname + search + hash;
	};
	
	//test if a given url (string) is a path
	//NOTE might be exceptionally naive
	var isPath = function( url ) {
		return ( /\// ).test( url );
	};
	
	var findClosestLink = function( ele )	{
		while ( ele ) {
			// Look for the closest element with a nodeName of "a".
			// Note that we are checking if we have a valid nodeName
			// before attempting to access it. This is because the
			// node we get called with could have originated from within
			// an embedded SVG document where some symbol instance elements
			// don't have nodeName defined on them, or strings are of type
			// SVGAnimatedString.
			if ( ( typeof ele.nodeName === "string" ) && ele.nodeName.toLowerCase() === "a" ) {
				break;
			}
			ele = ele.parentNode;
		}
		return ele;
	};
	
	var _parseBody = function(html) {
		var page = $(html.split( /<\/?body[^>]*>/gmi )[1] || "" )
		return page;
	};

	var _fixLink = function(_, elem) {
	    var href = $(elem).attr("href");
		if ( href.search( "#" ) !== -1 ) {
		    href = href.replace( /[^#]*#/, "" );
		    if ( !href ) {
			//link was an empty hash meant purely
			//for interaction, so we ignore it.
			return;
		    } else if ( !isPath( href ) ) {
			//we have a simple id so use the documentUrl as its base.
			href = makeUrlAbsolute( "#" + href, baseUrl );
		    }
		}

		if ( isPath( href ) ) {
		    //we have a path so make it the href we want to load.
		    href = makeUrlAbsolute( href, baseUrl );
		}

		$(elem).attr("href", href);
		//console.debug("rewrite: " + href);
	};
	
	var _loadSuccess = function(data, textStatus, jqXHR) {
		console.debug("loadSuccess");
		var allContent = $.htmlDoc(data);
		var content = _parseBody(data).not("script").find("script").remove().end();
		var cssContent = allContent.find("link[href]").addBack("link[href]");
		var scriptContent = allContent.find("script[src]").addBack("script[src]");
		var inlineContent = allContent.find("script").addBack("script").not("script[src]");
		//$(document.body).empty();

		var lazycss = cssContent.each(_fixLink).map(function() { return this.href; }).get();
		LazyLoad.css(lazycss);

		content.find("a[href],link[href]").addBack("a[href],link[href]").each(_fixLink);
		content.prependTo(document.body);

		var lazyjs = [];
		scriptContent.each(function() {
		    var src = $(this).attr("src");
                    if ( isPath( src ) ) {
                        src = makeUrlAbsolute( src, baseUrl );
                    }

		    //console.debug("inject: " + src);
		    lazyjs.push(src);
		});

		LazyLoad.js(lazyjs, function() {
		    inlineContent.appendTo(document.body);
		});
	};
	
	var _loadError = function(jqXHR, textStatus, errorThrown) {
		console.error(textStatus + " - " + errorThrown);
	};
	
	var _loadChat = function(url) {
		url = url || "http://www.destiny.gg/embed/chat";
		console.debug("loadchat");
		// Load the new content.
		$.ajax({
			url: url,
			type: "get",
			cache: false,
			data: null,
			contentType: null,
			dataType: "html",
			success: _loadSuccess,
			error: _loadError
		});
	};

	// phonegap ready
	var pgReady = $.Deferred();
	document.addEventListener("deviceready", pgReady.resolve, false);
	$.when(pgReady).then(function () {
	    console.debug("pgReady");
	    _loadChat();
	});
	
	$(document).ready(function() {
		// click routing - direct to HTTP or Ajax, accordingly
		$(document).bind( "click", function( event ) {
			if ( event.isDefaultPrevented() ) {
				return;
			}

			var link = findClosestLink( event.target ),
				$link = $( link );
			
			if ( !link || event.which > 1 ) {
				return;
			}
			
			var href = makeUrlAbsolute( $link.attr( "href" ) || "#", baseUrl );

                        if ( href.search( "#" ) !== -1 ) {
                            href = href.replace( /[^#]*#/, "" );
                            if ( !href ) {
                                //link was an empty hash meant purely
                                //for interaction, so we ignore it.
                                event.preventDefault();
                                return;
                            } else if ( isPath( href ) ) {
                                //we have a path so make it the href we want to load.
                                href = makeUrlAbsolute( href, baseUrl );
                            } else {
                                //we have a simple id so use the documentUrl as its base.
                                href = makeUrlAbsolute( "#" + href, baseUrl );
                            }
                        }

                        var hostname = parseUrl(href).hostname;

			// Should we handle this link, or let the browser deal with it?
			//var useDefaultUrlHandling = $link.is( "[rel='external']" ) || $link.is( "[target]" );
			var useInAppBrowser = /destiny.gg$/.test(hostname);
			var target = useInAppBrowser ? "_blank" : "_system";
			
			var iab = window.open(href, target);
			iab.addEventListener("exit", function() { 
				console.debug("iab exit");
				window.location.reload(true);
			});
			event.preventDefault();
		});
	});
})(jQuery, window, document);
jQuery.noConflict();
