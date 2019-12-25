package Model;

/**
 * Created by Singhabahu-PC on 8/19/2019.
 */
public class ItemTM
{
    String code,description;
    double qty,price;

    public ItemTM(String code, String description, double qty, double price)
    {
        this.code = code;
        this.description = description;
        this.qty = qty;
        this.price = price;
    }

    public String getCode()
    {
        return code;
    }

    public void setCode(String code)
    {
        this.code = code;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public double getQty()
    {
        return qty;
    }

    public void setQty(double qty)
    {
        this.qty = qty;
    }

    public double getPrice()
    {
        return price;
    }

    public void setPrice(double price)
    {
        this.price = price;
    }
}
