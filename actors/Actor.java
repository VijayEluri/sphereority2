package actors;

import common.*;

/**
 * Represents any object that can be displayed on the screen.
 * This includes players, projectiles, etc.
 * @author dvanhumb
 */
public abstract class Actor implements ActorConstants
{
	/**
	 * The current position of this Actor
	 */
	private Position position;
	/**
	 * The current velocity of this Actor
	 */
	private Position velocity;
	/**
	 * This Actor's size
	 */
	private Position size;
	/**
	 * Indicates whether this actor is done and needs to be removed from the system
	 */
	private boolean needsRemoving;
    /**
     * The team to which this Actor belongs
     */
    private byte team;
	
	public Actor()
	{
		position = new Position();
		velocity = new Position();
		size = new Position(1, 1);
		needsRemoving = false;
        team = TEAMLESS_CODE;
	}
    
    public Actor(Position position)
    {
        this();
        this.position = position;
    }
    
    public Actor(Position position, Position size)
    {
        this();
        this.position = position;
        this.size = size;
    }
	
	/**
	 * @return The current position of this Actor
	 */
	public Position getPosition()
	{
		return position;
	}
	
	/**
	 * @return The current speed of this Actor
	 */
	public Position getVelocity()
	{
		return velocity;
	}
	
	/**
	 * @return The size of this Actor
	 */
	public Position getSize()
	{
		return size;
	}
	
	/**
	 * Returns true if we're finished with this Actor and it needs to
	 * be removed from the system.
	 * @return Returns true if we need to remove this Actor from the system.
	 */
	public boolean getNeedsRemoving()
	{
		return needsRemoving;
	}
    
    /**
     * The code for the team this Actor is on
     * @return The raw team code
     */
    public byte getTeam()
    {
        return team;
    }
    
    /**
     * Returns true if this Actor is on team A
     * @return
     */
    public boolean isTeamA()
    {
        return team == TEAM_A_CODE;
    }
    
    /**
     * Returns true if this Actor is on team B
     * @return
     */
    public boolean isTeamB()
    {
        return team == TEAM_B_CODE;
    }
    
    /**
     * Returns true if this Actor has no team
     * @return
     */
    public boolean isTeamless()
    {
        return !isTeamA() && !isTeamB();
    }
    
    /**
     * Move this Actor in a direction
     * @param dist  The direction to move by
     * @param dTime  The amount of time that's elapsed since last timer tick
     */
    public void move(Position dist, float dTime)
    {
       position = position.move(dist, dTime); 
    }
    
    /**
     * Apply the current velocity to this Actor's Position
     * @param dTime  The amount of time that elapsed since the last timer tick
     */
    public void move(float dTime)
    {
        move(velocity, dTime);
    }
}
