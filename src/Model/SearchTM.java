package Model;

/**
 * Created by Singhabahu-PC on 8/21/2019.
 */
public class SearchTM
{
    private String orderId;
    private String date ;
    private double total;
    private String customerId;
    private String customerName;

    public SearchTM(String orderId, String date, double total, String customerId, String customerName)
    {
        this.orderId = orderId;
        this.date = date;
        this.total = total;
        this.customerId = customerId;
        this.customerName = customerName;
    }

    public SearchTM()
    {
    }

    public double getTotal()
    {
        return total;
    }

    public void setTotal(double total)
    {
        this.total = total;
    }

    public String getOrderId()
    {
        return orderId;
    }

    public void setOrderId(String orderId)
    {
        this.orderId = orderId;
    }

    public String getDate()
    {
        return date;
    }

    public void setDate(String date)
    {
        this.date = date;
    }

    public String getCustomerId()
    {
        return customerId;
    }

    public void setCustomerId(String customerId)
    {
        this.customerId = customerId;
    }

    public String getCustomerName()
    {
        return customerName;
    }

    public void setCustomerName(String customerName)
    {
        this.customerName = customerName;
    }

    @Override
    public String toString()
    {
        return "SearchTM{" +
                "orderId='" + orderId + '\'' +
                ", date='" + date + '\'' +
                ", total=" + total +
                ", customerId='" + customerId + '\'' +
                ", customerName='" + customerName + '\'' +
                '}';
    }
}
