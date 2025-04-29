package server.ui;

import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import java.io.File;
import java.util.Locale;

/**
 * [Description]
 *
 * @author Garret Reichenbach
 */
public class FileTreeModel implements TreeModel {

	private final File root;

	public FileTreeModel(File root) {
		this.root = root;
	}

	@Override
	public File getRoot() {
		return root;
	}

	@Override
	public File getChild(Object parent, int index) {
		File directory = (File) parent;
		String[] children = directory.list();
		if(children == null || index >= children.length) return null;
		else return new File(directory, children[index]);
	}

	@Override
	public int getChildCount(Object parent) {
		File file = (File) parent;
		if(file.isDirectory()) {
			String[] children = file.list();
			if(children == null) return 0;
			else {
				//There's too many files in the server-database folder to show, we use the Database Manager window for this.
				if(file.getParentFile().getName().equals("server-database")) return 0;
				return children.length;
			}
		} else return 0;
	}

	@Override
	public boolean isLeaf(Object node) {
		File file = (File) node;
		return file.isFile();
	}

	@Override
	public void valueForPathChanged(TreePath path, Object newValue) {

	}

	@Override
	public int getIndexOfChild(Object parent, Object child) {
		File directory = (File) parent;
		File file = (File) child;
		String[] children = directory.list();
		if(directory.isDirectory() && children != null) {
			for(int i = 0; i < children.length; i++) {
				if(file.getName().equals(children[i])) return i;
			}
		}
		return -1;
	}

	@Override
	public void addTreeModelListener(TreeModelListener l) {

	}

	@Override
	public void removeTreeModelListener(TreeModelListener l) {

	}

	public boolean canEdit(File file) {
		String name = file.getName().toLowerCase(Locale.ENGLISH);
		return name.endsWith(".cfg") || name.endsWith(".properties") || name.endsWith(".txt") || name.endsWith(".json") || name.endsWith(".xml") || name.endsWith(".yml") || name.endsWith(".sh") || name.endsWith(".bat") || name.endsWith(".cmd") || name.endsWith(".command");
	}

	public static class FileTreeCellRenderer extends DefaultTreeCellRenderer {

		private final char separator = File.separatorChar;

		@Override
		public void setText(String text) {
			int index = text.lastIndexOf(separator);
			if(index != -1) text = text.substring(index + 1);
			super.setText(text);
		}

		@Override
		public String getText() {
			String text = super.getText();
			if(text != null) {
				int index = text.lastIndexOf(separator);
				if(index != -1) text = text.substring(index + 1);
			}
			return text;
		}
	}
}
