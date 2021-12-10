package dev.geco.gsit.api;

import dev.geco.gsit.GSitMain;

public class GSitAPI {
    
    private final GSitMain g = GSitMain.getInstance();
    
    /**
    * Returns the Instance
    * <p>
    * @since 1.0.0
    * <p>
    * @author Gecolay
    */
    public GSitMain getInstance() { return g != null ? g : GSitMain.getInstance(); }


    
}