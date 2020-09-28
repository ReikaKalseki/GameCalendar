package Reika.GameCalendar.GUI;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import Reika.GameCalendar.IO.FileIO;

import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListView;
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
		cb.pause();
		System.out.println("Saving persisted GUI state for "+cb);
		this.storageFolder.mkdirs();
		for (String s : cb.getOptionNodeNames()) {
			Node n = cb.getOptionNode(s);
			if (n == null) {
				System.err.println("Could not find node mapped to id '"+s+"'!");
				continue;
			}
			if (!cb.shouldNodePersist(n))
				continue;
			NodeState ns = new NodeState(s);
			ns.load(n);
			ns.writeFile();
		}
		cb.unpause();
	}

	public void load(C cb) throws IOException {
		cb.pause();
		if (this.storageFolder.exists()) {
			System.out.println("Loading persisted GUI state for "+cb);
			for (File f : this.storageFolder.listFiles()) {
				String s = f.getName().substring(0, f.getName().length()-4);
				NodeState ns = new NodeState(s);
				ns.readFile(f);
				Node n = cb.getOptionNode(s);
				if (n == null) {
					System.err.println("Could not find node mapped to id '"+s+"'!");
					continue;
				}
				if (!cb.shouldNodePersist(n))
					continue;
				ns.write(n);
			}
		}
		else {
			System.out.println("No persisted GUI state for "+cb+" exists to load.");
		}
		cb.unpause();
	}

	private class NodeState {

		private final String id;
		private final String filename;

		private final HashMap<String, String> data = new HashMap();

		private NodeState(String s) {
			id = s;
			filename = id+".gui";
		}

		private void load(Node n) {
			if (n instanceof CheckBox) {
				this.data.put("selected", String.valueOf(((CheckBox)n).isSelected()));
			}
			if (n instanceof Toggle) {
				this.data.put("selected", String.valueOf(((Toggle)n).isSelected()));
			}
			if (n instanceof TextInputControl) {
				this.data.put("text", ((TextInputControl)n).getText());
			}
			if (n instanceof Slider) {
				this.data.put("pos", String.valueOf(((Slider)n).getValue()));
			}
			if (n instanceof ListView) {
				ListView lv = (ListView)n;
				List<Integer> li = new ArrayList(lv.getSelectionModel().getSelectedIndices());
				Collections.sort(li);
				StringBuilder sb = new StringBuilder();
				for (int i = 0; i < li.size(); i++) {
					sb.append(li.get(i));
					if (i < li.size()-1)
						sb.append(",");
				}
				this.data.put("entries", sb.toString());
			}
		}

		private void write(Node n) {
			if (n instanceof CheckBox) {
				((CheckBox)n).setSelected(Boolean.parseBoolean(data.get("selected")));
			}
			if (n instanceof Toggle) {
				((Toggle)n).setSelected(Boolean.parseBoolean(data.get("selected")));
			}
			if (n instanceof TextInputControl) {
				((TextInputControl)n).setText(data.get("text"));
			}
			if (n instanceof Slider) {
				((Slider)n).setValue(Double.parseDouble(data.get("pos")));
			}
			if (n instanceof ListView) {
				ListView lv = (ListView)n;
				String[] numbers = this.data.get("entries").split(",");
				lv.getSelectionModel().clearSelection();
				for (String s : numbers) {
					lv.getSelectionModel().select(Integer.parseInt(s));
				}
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
