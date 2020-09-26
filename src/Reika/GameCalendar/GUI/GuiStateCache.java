package Reika.GameCalendar.GUI;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import Reika.GameCalendar.IO.FileIO;

import javafx.scene.Node;
import javafx.scene.control.Slider;
import javafx.scene.control.TextInputControl;
import javafx.scene.control.Toggle;

public class GuiStateCache<C extends ControllerBase> {

	private final File storageFolder;

	public GuiStateCache(String f) {
		this(new File(f));
	}

	public GuiStateCache(File f) {
		storageFolder = f;
	}

	public void save(C cb) throws IOException {
		this.storageFolder.mkdirs();
		for (String s : cb.getNodeNames()) {
			Node n = cb.getNode(s);
			NodeState ns = new NodeState(s);
			ns.load(n);
			ns.writeFile();
		}
	}

	public void load(C cb) throws IOException {
		for (File f : this.storageFolder.listFiles()) {
			String s = f.getName().substring(0, f.getName().length()-4);
			NodeState ns = new NodeState(s);
			ns.readFile(f);
			Node n = cb.getNode(s);
			ns.write(n);
		}
	}

	private class NodeState {

		private final String filename;

		private final HashMap<String, String> data = new HashMap();

		private NodeState(String s) {
			filename = s+".gui";
		}

		private void load(Node n) {
			if (n instanceof Toggle) {
				this.data.put("selected", String.valueOf(((Toggle)n).isSelected()));
			}
			if (n instanceof TextInputControl) {
				this.data.put("text", ((TextInputControl)n).getText());
			}
			if (n instanceof Slider) {
				this.data.put("pos", String.valueOf(((Slider)n).getValue()));
			}
		}

		private void write(Node n) {
			if (n instanceof Toggle) {
				((Toggle)n).setSelected(Boolean.parseBoolean(data.get("selected")));
			}
			if (n instanceof TextInputControl) {
				((TextInputControl)n).setText(data.get("text"));
			}
			if (n instanceof Slider) {
				((Slider)n).setValue(Double.parseDouble(data.get("pos")));
			}
		}

		private void readFile(File f) throws IOException {
			this.data.clear();
			ArrayList<String> li = FileIO.getFileAsLines(f);
			for (String s : li) {
				int idx = s.indexOf(':');
				if (idx < 0) {
					System.err.println("GUI State File '"+f.getName()+"' has invalid line '"+s+"'!");
				}
				else {
					if (idx > 0 && idx < s.length()-1)
						data.put(s.substring(0, idx), s.substring(idx+1));
					else
						System.err.println("GUI State File '"+f.getName()+"' has valueless line '"+s+"'!");
				}
			}
		}

		private void writeFile() throws IOException {
			if (this.data.isEmpty())
				return;
			File f = this.getFile();
			if (f.exists())
				f.delete();
			f.createNewFile();
			ArrayList<String> li = new ArrayList();
			for (Entry<String, String> e : data.entrySet()) {
				String line = e.getKey()+":"+e.getValue();
				li.add(line);
			}
			FileIO.writeLinesToFile(f, li);
		}

		private File getFile() {
			return new File(storageFolder, filename);
		}

	}

}
