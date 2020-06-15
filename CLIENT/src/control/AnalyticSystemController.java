package control;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.ResourceBundle;

import Entity.Car;
import Entity.Customer;
import Entity.CustomerType;
import Entity.FuelCompany;
import Entity.FuelType;
import Entity.HomeFuelOrder;
import Entity.Order;
import Entity.PurchasePlan;
import Entity.RankingCustomer;
import Entity.Request;
import gui.OrderSummeryForm;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;


public class AnalyticSystemController implements Initializable {
		@FXML
		private	ComboBox<String> fueltypecombo;
		@FXML
		private	ComboBox<String> fuelhourscombo;
		@FXML
		private	ComboBox<String> typecombo;
		@FXML
		private TableView<RankingCustomer> tableview;
		@FXML
		private TableColumn<RankingCustomer, String> rankcol;
		@FXML
		private TableColumn<RankingCustomer, String> customeridcol;
		@FXML
		private TableColumn<RankingCustomer, String> typecol;
		@FXML
		private Button recalcbutton;
		@FXML
		private Text customercount;
		@FXML
		private Button homepagebutton;
		@FXML
		private Button logoutbutton;
		private ClientController client;
		private FuelType[] fueltypearr;
		private ObservableList<RankingCustomer> olist;
		private FilteredList<RankingCustomer> filteredData;
		private String tempStr;
		@FXML
		private void onRecalculateClick(ActionEvent event){
			Request r = new Request("analytic recalculate");
			try {
				client.sendToServer(r);
				client.displayAlert(true, null);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		@FXML
		private void onLogOutClick(ActionEvent event) throws Exception {
			client.restartApplication(null);
		}

		@FXML
		private void onFuelHoursSelected(ActionEvent event) throws Exception {
			String temp = typecombo.getSelectionModel().getSelectedItem();
			String msg = "analytic pull "+temp;
			client.sendToServer(new Request(msg));
		}
		@FXML
		private void onFuelTypeSelected(ActionEvent event) throws Exception {
			String temp = typecombo.getSelectionModel().getSelectedItem();
			String msg = "analytic pull "+temp;
			client.sendToServer(new Request(msg));
		}
		@FXML
		private void onHomePageClick(ActionEvent event) throws Exception {
			client.getMainPage().start(client.getMainStage());
			client.setClientIF(client.getMainPage());
		}

		public AnalyticSystemController(ClientController client) {
			this.client = client;
		}
		
		@Override
		public void initialize(URL arg0, ResourceBundle arg1) {
			try {
				getTableDataFromDB();
				fuelhourscombo.getItems().add("00:00-02:00");
				fuelhourscombo.getItems().add("02:00-04:00");
				fuelhourscombo.getItems().add("04:00-06:00");
				fuelhourscombo.getItems().add("06:00-08:00");
				fuelhourscombo.getItems().add("08:00-10:00");
				fuelhourscombo.getItems().add("10:00-12:00");
				fuelhourscombo.getItems().add("12:00-14:00");
				fuelhourscombo.getItems().add("14:00-16:00");
				fuelhourscombo.getItems().add("16:00-18:00");
				fuelhourscombo.getItems().add("18:00-20:00");
				fuelhourscombo.getItems().add("20:00-22:00");
				fuelhourscombo.getItems().add("22:00-00:00");
				typecombo.getItems().add("Private Customer");
				typecombo.getItems().add("Business Customer");
				
				typecombo.valueProperty().addListener((obs, oldItem, newItem) -> {
					tempStr = typecombo.getSelectionModel().getSelectedItem();
					if(tempStr.equals("Private Customer")) tempStr = "privatecustomer";
					else tempStr = "businesscustomer";
					filteredData.setPredicate(person -> {
						CustomerType ct = CustomerType.valueOf(tempStr);
						
						if (person.getCustomerType().equals(ct)) {
							return true; // Filter matches first name.
						}
						return false; // Does not match.
					});
					tableview.refresh();
		        });
				
				fueltypecombo.valueProperty().addListener((obs, oldItem, newItem) -> {
					tempStr = fueltypecombo.getSelectionModel().getSelectedItem();
					filteredData.setPredicate(person -> {		
						List<FuelType> list = person.getFuelTypes();
						for(FuelType f : list) {
							if(f.getName().equals(tempStr))
								return true;
						}
						return false; // Does not match.
					});
					tableview.refresh();
		        });
				fuelhourscombo.valueProperty().addListener((obs, oldItem, newItem) -> {
					tempStr = fuelhourscombo.getSelectionModel().getSelectedItem();
					String[] str = tempStr.split("-"); //02:00, 04:00
					String[] firstarr = str[0].split(":"); //02, 00
					String[] secondarr = str[1].split(":"); //04, 00
					Integer middle = Integer.parseInt(secondarr[0])-Integer.parseInt(firstarr[0]);
					Integer first = Integer.parseInt(firstarr[0]);
					Integer second = Integer.parseInt(secondarr[0]);
					filteredData.setPredicate(person -> {		
						List<LocalTime> list = person.getFuelHours();
						for(LocalTime f : list) {
							if(f.getHour() == middle || f.getHour() == first || f.getHour() == second)
								return true;
						}
						return false; // Does not match.
					});
					tableview.refresh();
		        });
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		public void getTableDataFromDB() throws IOException {
			String msg = "pull FuelType";
			Request req = new Request(msg);
			client.sendToServer(req);
			msg = "analytic pull";
			req = new Request(msg);
			client.sendToServer(req);
		}
		
		//Handle objects sent from UI
		@SuppressWarnings("unchecked")
		public boolean getMessageFromUI(Object obj) {
			if(obj instanceof List) {
				List<Object> temp = (List<Object>)obj;
				if(temp.isEmpty()) return false;
				if(temp.get(0) instanceof List) {
					List<List<Object>> list = (List<List<Object>>)obj;
					fueltypearr = new FuelType[list.size()];
					int i;
					for(i = 0; i<list.size(); i++) {
						fueltypearr[i] = createFuelTypeFromList(list.get(i));
						if(fueltypearr[i].getStatus().equals("ACTIVE")) {
							fueltypecombo.getItems().add(fueltypearr[i].getName());
						}
					}
					tableview.refresh();
				}
				else if(temp.get(0) instanceof RankingCustomer) {
					List<RankingCustomer> list = new ArrayList<RankingCustomer>();
					for(Object o : temp)
						list.add((RankingCustomer)o);
					setTableDataFromDB(list);
					return true;
				}
			}
			return false;
		}
		public FuelType createFuelTypeFromList(List<Object> list) {
			FuelType newVal;
			if(list.size() > 0) {
				newVal = new FuelType(
					list.get(0).toString(),
					Double.parseDouble(list.get(1).toString()),
					Float.parseFloat(list.get(2).toString()),
					Double.parseDouble(list.get(3).toString()),
					list.get(4).toString());
				return newVal;
			}
			return null;
		}
		public void setTableDataFromDB(List<RankingCustomer> list) {
			olist = FXCollections.observableArrayList();
			for(RankingCustomer l : list)
				olist.add(l);
			//JavaFX
			//Injection
			rankcol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTotalRank().toString()));
			customeridcol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCustomerID().toString()));
			typecol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCustomerType().toString()));
			Platform.runLater(new Runnable() {
			    @Override
			    public void run() {
					filteredData = new FilteredList<>(olist, p -> true);
					tableview.setItems(filteredData);
					rankcol.setSortType(TableColumn.SortType.DESCENDING);
					tableview.getSortOrder().add(rankcol);
					tableview.sort();
					customercount.setText(String.valueOf(tableview.getItems().size()));
			    }
			});
		}
}