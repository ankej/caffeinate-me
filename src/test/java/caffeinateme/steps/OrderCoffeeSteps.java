package caffeinateme.steps;

import caffeinateme.model.*;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.DataTableType;
import io.cucumber.java.ParameterType;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class OrderCoffeeSteps {
    ProductCatalog catalog = new ProductCatalog();
    CoffeeShop coffeeShop = new CoffeeShop(catalog);
    Order order;
    Customer customer;

    @Given("{} is a CaffeinateMe customer")
    public void a_caffeinate_me_customer_named(String customerName) {
        customer = coffeeShop.registerNewCustomer(customerName);
    }

    @Given("Cathy is {int} metres from the coffee shop")
    public void cathy_is_metres_from_the_coffee_shop(Integer distanceInMetres) {
        customer.setDistanceFromShop(distanceInMetres);
    }

    @ParameterType("\"[^\"]*\"")
    public Order order(String orderedProduct) {
        return Order.of(1, orderedProduct).forCustomer(customer);
    }

    @When("Cathy orders a {order}")
    public void cathy_orders_a(Order newOrder) {
        this.order = newOrder;
        customer.placesAnOrderFor(this.order).at(coffeeShop);
    }

    @When("Cathy orders a {order} with a comment {string}")
    public void cathy_orders_with_comment(Order newOrder, String comment) {
        this.order = newOrder.withComment(comment);
        customer.placesAnOrderFor(this.order).at(coffeeShop);
    }

    @Then("Barry should receive the order")
    public void barry_should_receive_the_order() {
        assertThat(coffeeShop.getPendingOrders()).contains(order);
    }

    @Then("^Barry should know that the order is (.*)")
    public void barry_should_know_that_the_order_is(OrderStatus expectedStatus) {
        Order cathysOrder = coffeeShop.getOrderFor(customer)
                .orElseThrow(() -> new AssertionError("No order found!"));
        assertThat(cathysOrder.getStatus()).isEqualTo(expectedStatus);
    }

    @Then("the order should have the comment {string}")
    public void order_should_have_comment(String comment) {
        Order order = coffeeShop.getOrderFor(customer).get();
        assertThat(order.getComment()).isEqualTo(comment);
    }

    @DataTableType
    public OrderItem orderItem(Map<String,String> row) {
        return new OrderItem(row.get("Product"), Integer.parseInt(row.get("Quantity")));
    }
/*    @Given("Cathy has placed an order for the following items:")
    @When("Cathy places an order for the following items:")*/
    @When("^Cathy (?:has placed|places) an order for the following items:")
    public void cathyPlacesAnOrderForTheFollowingItems(List<OrderItem> orderItems) {
        this.order = new Order(orderItems, customer);
        coffeeShop.placeOrder(this.order);
    }

    @And("the order should contain {int} line items")
    public void theOrderShouldContainLineItems(int expectedNumberOfLineItems) {
        Order order = coffeeShop.getOrderFor(customer).get();
        assertThat(order.getItems()).hasSize(expectedNumberOfLineItems);
    }

    @And("the order should contain the following products:")
    public void theOrderShouldContain(List<String> expectedProducts) {
        Order order = coffeeShop.getOrderFor(customer).get();
        List<String> productItems = order.getItems().stream().map(item -> item.product()).collect(Collectors.toList());
        assertThat(productItems).hasSameElementsAs(expectedProducts);
    }

    @DataTableType
    public ProductPrice productPrice(Map<String, String> entry){
        return new ProductPrice(entry.get("Product"),
                                Double.parseDouble(entry.get("Price")));
    }
    @Given("the following prices:")
    public void setupCatalog(List<ProductPrice> productPrices){
        catalog.addProductsWithPrices(productPrices);
    }
    Receipt receipt;
    @When("she/he asks for a receipt")
    public void sheAsksForAReceipt() {
        receipt = coffeeShop.getReceiptFor(customer);
    }

    @Then("she should receive a receipt totalling:")
    public void sheShouldReceiveAReceiptTotalling(Map<String, Double> receiptTotals) {
        double serviceFee = receiptTotals.get("Service Fee");
        double subtotal = receiptTotals.get("Subtotal");
        double total = receiptTotals.get("Total");
        assertThat(receipt.serviceFee()).isEqualTo(serviceFee);
        assertThat(receipt.subtotal()).isEqualTo(subtotal);
        assertThat(receipt.total()).isEqualTo(total);
    }
    @DataTableType
    //must be public to be accessable by cucumber runner class
    public ReceiptLineItem receiptLineItem(Map<String, String> itemData) {
        return new ReceiptLineItem(
                itemData.get("Product"),
                Integer.parseInt(itemData.get("Quantity")),
                        Double.parseDouble(itemData.get("Price"))
                );
    }
    @And("the receipt should contain the line items:")
    public void theReceiptShouldContainTheLineItems(List<ReceiptLineItem> expectedItems) {
        assertThat(receipt.lineItems()).containsExactlyElementsOf(expectedItems);
    }
}
