package iristk.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JTree;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

public class RecordTreeView extends JTree  {

	public RecordTreeView(Record record) {
		super(new RecordTreeModel(record));
		//record.addListener((RecordTreeModel)getModel());
	}

	private static class RecordTreeModel implements TreeModel, RecordListener {

		private RecordNode root;
		private List<TreeModelListener> listeners = new ArrayList<TreeModelListener>();

		public RecordTreeModel(Record record) {
			this.root = new RecordNode("Record", record);
		}

		@Override
		public Object getRoot() {
			return root;
		}

		@Override
		public Object getChild(Object parent, int index) {
			return ((RecordNode)parent).getChild(index);
		}

		@Override
		public int getChildCount(Object parent) {
			return ((RecordNode)parent).getChildCount();
		}

		@Override
		public boolean isLeaf(Object node) {
			return ((RecordNode)node).isLeaf(); 
		}

		@Override
		public void valueForPathChanged(TreePath path, Object newValue) {
			// TODO Auto-generated method stub
		}

		@Override
		public int getIndexOfChild(Object parent, Object child) {
			return ((RecordNode)parent).getIndexOfChild(child);
		}

		@Override
		public void addTreeModelListener(TreeModelListener l) {
			listeners.add(l);
		}

		@Override
		public void removeTreeModelListener(TreeModelListener l) {
			listeners.remove(l);
		}

		@Override
		public void recordChanged() {
			for (TreeModelListener listener : listeners) {
				listener.treeStructureChanged(new TreeModelEvent(this, new Object[]{root}));
			}
		}
	}
	
	private static class RecordNode {
		
		private Object object;
		private String key;

		public RecordNode(String key, Object object) {
			this.key = key;
			this.object = object;
		}
		
		public boolean isLeaf() {
			return !(object instanceof Record);
		}

		public int getChildCount() {
			if (object instanceof Record)
				return ((Record)object).size();
			else
				return 0;
		}

		public int getIndexOfChild(Object child) {
			List<String> keys = new ArrayList<String>(((Record)object).keySet());
			Collections.sort(keys);
			for (int i = 0; i < ((Record)object).size(); i++) 
				if (((Record)object).get(keys.get(i)) == ((RecordNode)child).object)
					return i;
			return -1;
		}

		public Object getChild(int index) {
			List<String> keys = new ArrayList<String>(((Record)object).keySet());
			Collections.sort(keys);
			String key = keys.get(index);
			return new RecordNode(key, ((Record)object).get(key));
		}

		@Override
		public String toString() {
			if (object instanceof Record) {
				return key;
			} else {
				return key + "=" + object.toString();
			}
		}
		
	}

}
