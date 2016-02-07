package ext.edu.ucsf.rbvi.strucviz2;

/**
 * This object maintains the relationship between Chimera objects and Cytoscape
 * objects.
 */
public class StructureSettings
{

  // @Tunable(description = "Path to UCSF Chimera application", gravity = 4.0)
  public String chimeraPath = null;

  public StructureSettings(StructureManager manager)
  {

    chimeraPath = manager.getCurrentChimeraPath(null);

    // This seems a little strange, but it has to do with the order of tunable
    // interceptor
    // handling. We need to set these selectors in our structure manager and
    // dynamically
    // pull the data out as needed....
    manager.setStructureSettings(this);
  }

  public String getChimeraPath()
  {
    return chimeraPath;
  }
}
