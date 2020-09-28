package Reika.GameCalendar.GUI;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import Reika.GameCalendar.VideoExport.VideoRenderer;

import javafx.application.HostServices;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.Slider;
import javafx.scene.control.SplitPane;
import javafx.scene.control.SplitPane.Divider;
import javafx.scene.control.TextInputControl;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Toggle;
import javafx.scene.layout.Region;

public abstract class ControllerBase implements EventHandler<ActionEvent> {

	private Parent rootNode;

	private final HashSet<String> nameSet = new HashSet();
	private final HashSet<String> optionSet = new HashSet();
	private final HashMap<Object, NodeWrapper> allNodes = new HashMap();
	private final HashMap<String, NodeWrapper> optionNodes = new HashMap();
	private final HashMap<String, NodeWrapper> buttons = new HashMap();
	private final HashMap<String, NodeWrapper> listSelects = new HashMap();
	private final HashMap<String, NodeWrapper> textInputNodes = new HashMap();

	private HostServices host;

	private boolean pauseUpdates = false;

	protected void preInit(Parent root) {
		rootNode = root;
	}

	@FXML
	public void initialize() {
		if (rootNode == null)
			throw new RuntimeException("Preinit was never run!");

		this.addWrapperHooks(this.getAllNodes());

		for (Node n : JFXWindow.getRecursiveChildren(rootNode)) {
			if (n instanceof SplitPane) {
				SplitPane p = (SplitPane)n;
				//p.setMouseTransparent(true);
				for (Divider d : p.getDividers()) {
					d.positionProperty().addListener(new DividerLocker(d));
				}
				for (Node n2 : p.getChildrenUnmodifiable()) {
					if (n2 instanceof Region) {
						Region r = (Region)n2;
						double w = r.getWidth();
						double h = r.getHeight();
						r.setMaxWidth(w);
						r.setMinWidth(w);
						r.setMaxHeight(h);
						r.setMinHeight(h);
					}
				}
			}
		}
	}

	protected void postInit(HostServices host) {
		this.host = host;
	}

	/*
	private void dynamicizeTextBoxes(Parent p) {
		for (Node n : JFXWindow.getRecursiveChildren(p)) {
			if (n instanceof TextArea) {
				Node n2 = n.lookup(".text");
				if (n2 == null) {
					System.out.println("Node "+n+" has no text component?!");
					continue;
				}
				n2.boundsInLocalProperty().addListener((bounds, oldVal, newVal) -> {
					((TextArea)n).setPrefHeight(newVal.getHeight() + 10); // 10px vertical padding to prevent scrollbar from showing
				});
			}
		}
	}*/

	private void addWrapperHooks(Collection<NodeWrapper> li) {
		for (NodeWrapper n2 : li) {
			this.addHook(n2);
		}
	}

	private void addHook(NodeWrapper n2) {
		if (n2.object instanceof CheckBox) {
			((CheckBox)n2.object).setOnAction(this);
			optionNodes.put(n2.fxID, n2);
			optionSet.add(n2.fxID);
		}
		else if (n2.object instanceof Toggle) {
			optionNodes.put(n2.fxID, n2);
			optionSet.add(n2.fxID);
		}
		else if (n2.object instanceof ButtonBase) {
			((ButtonBase)n2.object).setOnAction(this);
			buttons.put(n2.fxID, n2);
		}
		else if (n2.object instanceof ChoiceBox) {
			((ChoiceBox)n2.object).setOnAction(this);
			//optionNodes.put(n2.fxID, n2);
		}
		else if (n2.object instanceof ComboBox) {
			((ComboBox)n2.object).setOnAction(this);
			//optionNodes.put(n2.fxID, n2);
		}
		else if (n2.object instanceof TextInputControl) {
			//((TextInputControl)n2.object).textProperty().addListener(this);
			optionSet.add(n2.fxID);
			textInputNodes.put(n2.fxID, n2);
		}
		else if (n2.object instanceof Slider) {
			optionNodes.put(n2.fxID, n2);
			optionSet.add(n2.fxID);
		}
		else if (n2.object instanceof ListView) {
			((ListView)n2.object).getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {
				@Override
				public void changed(ObservableValue observable, Object oldValue, Object newValue) {
					if (!pauseUpdates)
						ControllerBase.this.update(n2.fxID);
				}

			});
			listSelects.put(n2.fxID, n2);
			optionSet.add(n2.fxID);
		}
	}

	protected final void registerNode(String id, Node n) {
		if (nameSet.contains(id))
			throw new IllegalArgumentException("ID '"+id+"' is already occupied!");
		NodeWrapper nw = new NodeWrapper(id, n);
		allNodes.put(id, nw);
		nameSet.add(id);
		this.addHook(nw);
	}

	private void collectAllNodes() throws Exception {
		allNodes.clear();
		nameSet.clear();
		optionSet.clear();
		Field[] fd = this.getClass().getDeclaredFields();
		for (Field f : fd) {
			if (f.getAnnotation(Deprecated.class) != null)
				continue;
			if (f.getAnnotation(FXML.class) == null)
				continue;
			f.setAccessible(true);
			Object o = f.get(this);
			if (o instanceof Node) {
				NodeWrapper nw = new NodeWrapper(f.getName(), (Node)o);
				allNodes.put(o, nw);
				nameSet.add(nw.fxID);
			}
		}
	}

	protected final Collection<NodeWrapper> getAllNodes() {
		if (allNodes.isEmpty()) {
			try {
				this.collectAllNodes();
			}
			catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		return Collections.unmodifiableCollection(allNodes.values());
	}

	protected final Collection<NodeWrapper> getAllActiveNodes() {
		Collection<NodeWrapper> ret = new ArrayList();
		for (NodeWrapper n : this.getAllNodes()) {
			if (!n.object.isDisabled())
				ret.add(n);
		}
		return ret;
	}

	public final Collection<Parent> getSections() {
		Collection<Parent> ret = new ArrayList();
		for (NodeWrapper n : this.getAllNodes()) {
			if (n.object instanceof Parent)
				ret.add((Parent)n.object);
		}
		return ret;
	}

	public final void handle(ActionEvent event) {
		if (VideoRenderer.instance.isRendering())
			return;

		Object o = event.getSource();
		String id = allNodes.get(o).fxID;
		this.update(id);
		if (o instanceof Button) {
			this.onButtonClick(o, id);
		}
	}

	protected abstract void onButtonClick(Object o, String id);

	protected final Collection<TitledPane> getCollapsibleSections() {
		Collection<TitledPane> ret = new ArrayList();
		for (NodeWrapper n : this.getAllNodes()) {
			if (n.object instanceof TitledPane)
				ret.add((TitledPane)n.object);
		}
		return ret;
	}

	protected abstract void update(String fxID);

	public final HostServices getHost() {
		return host;
	}

	protected final Node getOption(String id) {
		return optionNodes.get(id).object;
	}

	protected final ListView getListView(String id) {
		return (ListView)listSelects.get(id).object;
	}

	protected final Collection<NodeWrapper> getOptionNodes() {
		return Collections.unmodifiableCollection(optionNodes.values());
	}

	protected final NodeWrapper getNode(Node n) {
		return allNodes.get(n);
	}

	public final Node getOptionNode(String id) {
		NodeWrapper n = optionNodes.get(id);
		if (n == null)
			n = listSelects.get(id);
		if (n == null)
			n = textInputNodes.get(id);
		return n != null ? n.object : null;
	}

	public final Set<String> getNodeNames() {
		return Collections.unmodifiableSet(nameSet);
	}

	public final Set<String> getOptionNodeNames() {
		return Collections.unmodifiableSet(optionSet);
	}

	protected final void replaceNode(Node rem, Node repl) {
		NodeWrapper nw = allNodes.remove(rem);
		optionNodes.remove(nw.fxID);
		buttons.remove(nw.fxID);
		listSelects.remove(nw.fxID);
		NodeWrapper nw2 = new NodeWrapper(nw.fxID, repl);
		allNodes.put(repl, nw2);
		this.addHook(nw2);
	}

	public final void pause() {
		pauseUpdates = true;
	}

	public final void unpause() {
		pauseUpdates = false;
	}

	public boolean shouldNodePersist(Node n) {
		return true;
	}

	protected static class NodeWrapper {

		public final String fxID;
		public final Node object;

		private NodeWrapper(String id, Node o) {
			fxID = id;
			object = o;
		}

	}

	private static class DividerLocker implements ChangeListener<Number> {

		private final Divider split;
		private final double position;

		public DividerLocker(Divider d) {
			split = d;
			position = d.getPosition();
		}

		@Override
		public void changed(ObservableValue<? extends Number> value, Number oldValue, Number newValue) {
			split.setPosition(position);
		}

	}

}
