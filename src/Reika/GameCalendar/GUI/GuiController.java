package Reika.GameCalendar.GUI;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

import org.eclipse.fx.drift.DriftFXSurface;

import Reika.GameCalendar.Data.ActivityCategory;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SplitPane;
import javafx.scene.control.SplitPane.Divider;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputControl;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;

public class GuiController implements EventHandler<ActionEvent>, ChangeListener {

	@FXML
	public GridPane root;

	@FXML
	public ListView<String> catList;

	@FXML
	public Label status;

	@FXML
	public Object renderer;

	private final HashMap<Object, NodeWrapper> allNodes = new HashMap();
	private final HashMap<Character, NodeWrapper> optionNodes = new HashMap();

	@FXML
	public void initialize() {
		this.addWrapperHooks(this.getAllNodes());
		catList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		catList.getSelectionModel().selectedItemProperty().addListener(this);

		for (Node n : Window.getRecursiveChildren(root)) {
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

	public void postInit() {
		renderer = new DriftFXSurface();
		this.dynamicizeTextBoxes(root);
	}

	private void dynamicizeTextBoxes(Parent p) {
		for (Node n : Window.getRecursiveChildren(p)) {
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
	}

	public void load() {
		catList.setItems(FXCollections.observableArrayList(ActivityCategory.getNameList()));
	}

	private void addWrapperHooks(Collection<NodeWrapper> li) {
		for (NodeWrapper n2 : li) {
			this.addHook(n2.object);
		}
	}

	private void addHooks(Collection<Node> li) {
		for (Node n2 : li) {
			this.addHook(n2);
		}
	}

	private void addHook(Node n2) {
		if (n2 instanceof ButtonBase) {
			((ButtonBase)n2).setOnAction(this);
		}
		else if (n2 instanceof ChoiceBox) {
			((ChoiceBox)n2).setOnAction(this);
		}
		else if (n2 instanceof ComboBox) {
			((ComboBox)n2).setOnAction(this);
		}
		else if (n2 instanceof TextInputControl) {
			((TextInputControl)n2).textProperty().addListener(this);
		}
	}

	private void collectAllNodes() throws Exception {
		allNodes.clear();
		Field[] fd = this.getClass().getDeclaredFields();
		for (Field f : fd) {
			if (f.getAnnotation(Deprecated.class) != null)
				continue;
			Object o = f.get(this);
			if (o instanceof Node) {
				allNodes.put(o, new NodeWrapper(f.getName(), (Node)o));
			}
		}
	}

	public Collection<NodeWrapper> getAllNodes() {
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

	public Collection<NodeWrapper> getAllActiveNodes() {
		Collection<NodeWrapper> ret = new ArrayList();
		for (NodeWrapper n : this.getAllNodes()) {
			if (!n.object.isDisabled())
				ret.add(n);
		}
		return ret;
	}

	public Collection<Parent> getSections() {
		Collection<Parent> ret = new ArrayList();
		for (NodeWrapper n : this.getAllNodes()) {
			if (n.object instanceof Parent)
				ret.add((Parent)n.object);
		}
		return ret;
	}

	public Collection<TitledPane> getCollapsibleSections() {
		Collection<TitledPane> ret = new ArrayList();
		for (NodeWrapper n : this.getAllNodes()) {
			if (n.object instanceof TitledPane)
				ret.add((TitledPane)n.object);
		}
		return ret;
	}

	public void handle(ActionEvent event) {
		this.update();

		Object o = event.getSource();
		/*
if (o instanceof ToggleGroup) {
	ToggleGroup tg = (ToggleGroup)event.getSource();
}

if (o instanceof ChoiceBox) {
	ChoiceBox b = (ChoiceBox)o;
	NodeWrapper nw = allNodes.get(b);
	switch(nw.fxID) {
		case "namespaces":
			String n = (String)b.getSelectionModel().getSelectedItem();
			this.load(Namespace.findNamespace(n));
			break;
	}
}*/
		if (o instanceof Button) {
			this.onButtonClick(allNodes.get(o).fxID);
		}
	}

	private void onButtonClick(String fxID) {
		switch(fxID) {

		}
		this.load();
	}

	@Override
	public void changed(ObservableValue observable, Object oldValue, Object newValue) {
		this.update();
	}

	private void updateElement(String elem) {

	}

	void update() {
		String e = catList.getSelectionModel().getSelectedItem();
		if (e != null) {

		}

		Window.getGUI().updateActiveSections();
	}

	public static class NodeWrapper {

		private final String fxID;
		private final Node object;

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
