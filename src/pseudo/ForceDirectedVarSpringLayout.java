package pseudo;

import prefuse.action.layout.graph.ForceDirectedLayout;
import prefuse.util.force.ForceSimulator;
import prefuse.visual.EdgeItem;
public class ForceDirectedVarSpringLayout extends ForceDirectedLayout {
	public int areaSize = 50 ;
	public ForceDirectedVarSpringLayout(String group,
			boolean enforceBounds)
	{
		super(group, enforceBounds);
	}

	public ForceDirectedVarSpringLayout(String group, ForceSimulator fsim,
			boolean enforceBounds, boolean runonce) {
		super(group, fsim, enforceBounds, runonce);

	}
	public ForceDirectedVarSpringLayout(String group, ForceSimulator fsim,
			boolean enforceBounds) {
		super(group, fsim, enforceBounds);
	}
	public ForceDirectedVarSpringLayout(String group, ForceSimulator fsim,
			boolean enforceBounds, int max) {
		super(group, fsim, enforceBounds);
		areaSize = max;
		
	}

	@Override
	public float getSpringLength(EdgeItem e){
		String weight = e.get("WEIGHT").toString();
		if(weight != null){
			return (float) (Math.log(Double.valueOf(weight)) * 80) ; // TODO: your function here
		} else {
			return 1.f;
		}
	}	
}

