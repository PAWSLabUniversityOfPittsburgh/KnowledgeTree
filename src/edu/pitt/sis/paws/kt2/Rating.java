/* Disclaimer:
 * 	Java code contained in this file is created as part of educational
 *    research and development. It is intended to be used by researchers of
 *    University of Pittsburgh, School of Information Sciences ONLY.
 *    You assume full responsibility and risk of lossed resulting from compiling
 *    and running this code.
 */
 
/** The object of a class represents a user's rating or a group of user ratings
 * @author Michael V. Yudelson
 * @version %I%, %G%
 * @since 1.5
 */

package edu.pitt.sis.paws.kt2;

import java.util.Vector;


/** The object of a class represents a user's rating or a group of user ratings
 * @author Michael V. Yudelson
 * @version %I%, %G%
 * @since 1.5
 */
 
public class Rating
{
	// CONSTANTS
	public static final String RATING_LABELS = 
		"['<span style=\"color:red;\">Highly Negative</span>','<span style=\"color:orange;\">Negative</span>','Neutral','<span style=\"color:olive;\">Positive</span>','<span style=\"color:green;\">Highly Positive</span>']";
	
	public static final String[] RATING_LABELS_ARRAY = {
		"<span style=\"color:red;\">Highly Negative</span>",
		"<span style=\"color:orange;\">Negative</span>",
		"Neutral",
		"<span style=\"color:olive;\">Positive</span>",
		"<span style=\"color:green;\">Highly Positive</span>"};
	
	
	/** Rating assigned. In case of the group of values - mean value
	 * @since 1.5
	 */
	private float rating_value;

	/** Anonymity flag
	 * @since 1.5
	 */
	private boolean is_anonymous;

	/** Comment supplied with the rating. In case of the group of values - empty
	 * @since 1.5
	 */
	private String comment;

	/** User that supplied rating. In case of group of values - null
	 * @since 1.5
	 */
	private User user;

	/** The node that was rated. In case of group of values - null
	 * @since 1.5
	 */
	private iNode rated_node;

	/** Group of ratings represented by this rating
	 * @since 1.5
	 */
	private Vector<Rating> ratings;

	/** Sum of ratings when object represents a group of rating values
	 * @since 1.5
	 */
	private int sum_ratings;

	/** Flag determining wheather this object represents a group of ratings
	 * @since 1.5
	 */
	private boolean is_group_rating;

	/**Constructor for a rating representing a single rating value
	 * @param _rating_value - rating value
	 * @param _comment - text of comment
	 * @param _user - user that supplied the rating
	 * @param _rated_node - the node being rated
	 * @since 1.5
	 */
	public Rating(float _rating_value, boolean _anonymous, String _comment, 
		User _user, iNode _rated_node)
	{
		if(_rating_value<1 || _rating_value>5)
		{
			System.out.println("!!! [KTree2] Rating.constructor illegal rating value(" + 
				_rating_value + "). Setting rating value to 0");
			rating_value = 0;
		}
		else
			rating_value = _rating_value;
			
		comment = (_comment!=null)?_comment:"";
		is_anonymous = _anonymous;
		user = _user;
		rated_node = _rated_node;
		ratings = null;
		is_group_rating = false;
	}

	/** Constructor for a rating representing a group of rating values
	 * @since 1.5
	 */
	public Rating(iNode _rated_node)
	{
		rating_value = 0;
		is_anonymous = false;
		comment = null;
		user = null;
		rated_node = _rated_node;
		ratings = new Vector<Rating>();;
		is_group_rating = true;
	}
	
	public boolean isGroupRating() { return is_group_rating; }
	
	public void addRating(Rating new_rating)
	{
		if(is_group_rating)
		{
			ratings.add(new_rating);
			sum_ratings += new_rating.getRatingValue();
			rating_value = (float)sum_ratings / ratings.size();
		}
		else
			System.out.println("!!! [KTree2] Rating.addRating ERROR! Trying to add rating to a single value rating.");
	}

	/** Method returns the value of the rating
	 * @return the value of the rating
	 * @since 1.5
	 */
	public float getRatingValue() { return rating_value; }
	
	/** Method sets the value of the rating
	 * @param _rating_value - the new value of the rating
	 * @since 1.5
	 */
	public void setRatingValue(float _rating_value)
	{
		if(is_group_rating)
		{
			System.out.println("!!! [KTree2] Rating.addRating ERROR! Trying to set a value for a group rating.");
			return;
		}
		else
			rating_value = _rating_value;
	}
	
	/** Method returns a group of rating values
	 * @return group of rating values
	 * @since 1.5
	 */
	public Vector<Rating> getRatings()
	{
		if(!is_group_rating)
			System.out.println("!!! [KTree2] Rating.addRating WARNING! You are " + 
				"requesting a group of rating values when rating is for a single user. " +
				"A null value will be returned.");
		return ratings;
	}

	/** Method returns the anonimity flag
	 * @return the anonimity flag
	 * @since 1.5
	 */
	public boolean getAnonymous() { return is_anonymous; }

	/** Method sets the anonimity flag
	 * @param _is_anonymous - the anonimity flag
	 * @since 1.5
	 */
	public void setAnonymous(boolean _is_anonymous)
	{
		if(is_group_rating)
		{
			System.out.println("!!! [KTree2] Rating.addRating ERROR! Trying to set an anonymity value for a group rating.");
			return;
		}
		else
			is_anonymous = _is_anonymous;
	}

	/** Method returns the user that supplied the rating
	 * @return the user that supplied the rating
	 * @since 1.5
	 */
	public User getUser()
	{
		if(is_group_rating)
			System.out.println("!!! [KTree2] Rating.addRating WARNING! You are " + 
				"requesting a user that supplied a group of ratings. " +
				"A null value will be returned.");
		return user;
	}

	/** Method returns the text that was supplied with the rating
	 * @return the text that was supplied with the rating
	 * @since 1.5
	 */
	public String getComment ()
	{
		if(is_group_rating)
		{
			System.out.println("!!! [KTree2] Rating.addRating WARNING! You are " + 
				"requesting the text of the comment that is related " +
				"to a group of ratings. A null value will be returned.");
			return null;
		}
		return comment;
	}

	/** Method sets the text that was supplied with the rating
	 * @param _comment - the text that was supplied with the rating
	 * @since 1.5
	 */
	public void setComment (String _comment)
	{
		if(is_group_rating)
		{
			System.out.println("!!! [KTree2] Rating.addRating WARNING! You are " + 
				"setting the text of the comment that is related " +
				"to a group of ratings. No changes will be made.");
		}
		comment = _comment;
	}

	/** Method returns the node that was rated
	 * @return the node that was rated
	 * @since 1.5
	 */
	public iNode getNode() { return rated_node; }
}
