package actors;

public abstract class WeightedActor extends Actor implements Animatible
{
    /**
     * The weight of this Actor. The hevier it is, the slower it is to react to motion
     */
    private float weight;
    
    public WeightedActor()
    {
        weight = DEFAULT_WEIGHT;
    }
    
    public WeightedActor(float weight)
    {
        this.weight = weight;
    }
    
    public float getWeight()
    {
        return weight;
    }
    
    public void animate(float curTime, float dTime)
    {
    	
    }
}
