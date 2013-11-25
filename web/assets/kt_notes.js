// REQUIRES PROTOTYPE


function initStars(a_star_on_img, a_star_off_img, a_ban_img, a_id, a_saved_state_id, a_changed_state_id, a_no_stars, a_remove_all)
{
	this.star_on_img = a_star_on_img;
	this.star_off_img = a_star_off_img;
	this.ban_img = a_ban_img;
	this.star_container_id = a_id;
	this.saved_state_id = a_saved_state_id;
	this.changed_state_id = a_changed_state_id;
	this.no_stars = a_no_stars;
	this.remove_all = a_remove_all;
	
	var bag = $(this.star_container_id);
	if(remove_all==1)
		bag.innerHTML = bag.innerHTML + "<a href=\"javascript:;\"><img title=\"remove rating\" border=\"0\" id=\"s0\" " + 
			"onMouseOver=\"starOn(0);\" onMouseOut=\"starOn($('rating-value').value);\" " +
			"onClick=\"$('rating-value').value=0;starOn($('rating-value').value);\" src=\"" + this.ban_img + "\"/></a>";
	var i=0;
	for(i=0; i<a_no_stars; i++)
		bag.innerHTML = bag.innerHTML + "";
	
}

/* Lighting up stars of the rater. img element must have id s#, where # is 0,1,2,3...,x.
 * 0 is for "remove rating" option. Icon should be named
 *
 */
function starOn(n_stars)
{
	to_zero = $A(document.getElementsByName('star'));
	to_zero.each(
		function(node){node.src=this.star_off_img;}
	);
	var i=0;
	for(i=0; i<n_stars; i++)
		$('s'+(i+1)).src =this.star_on_img;
}

			
			<a href="javascript:;"><img title="1/5 star" border="0" id="s1" name="star" src="star_off.gif" width="16" height="16" onMouseOver="starOn(1);" onMouseOut="starOn($('rating-value').value);" onClick="$('rating-value').value=1;starOn($('rating-value').value);"/></a>
			<a href="javascript:;"><img title="2/5 stars" border="0" id="s2" name="star" onMouseOver="starOn(2);" onMouseOut="starOn($('rating-value').value);"  onClick="$('rating-value').value=2;starOn($('rating-value').value);" src="star_off.gif" width="16" height="16"/></a>
			<a href="javascript:;"><img title="3/5 stars" border="0" id="s3" name="star" onMouseOver="starOn(3);" onMouseOut="starOn($('rating-value').value);"  onClick="$('rating-value').value=3;starOn($('rating-value').value);" src="star_off.gif" width="16" height="16"/></a>
			<a href="javascript:;"><img title="4/5 stars" border="0" id="s4" name="star" onMouseOver="starOn(4);" onMouseOut="starOn($('rating-value').value);"  onClick="$('rating-value').value=4;starOn($('rating-value').value);" src="star_off.gif" width="16" height="16"/></a>
			<a href="javascript:;"><img title="5/5 stars" border="0" id="s5" name="star" onMouseOver="starOn(5);" onMouseOut="starOn($('rating-value').value);"  onClick="$('rating-value').value=5;starOn($('rating-value').value);" src="star_off.gif" width="16" height="16"/></a>
