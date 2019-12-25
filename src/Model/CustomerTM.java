package Model;

/**
 * Created by Singhabahu-PC on 8/19/2019.
 */
public class CustomerTM
{
    String code,name,address;

    public CustomerTM(String code, String name, String address)
    {
        this.code = code;
        this.name = name;
        this.address = address;
    }

    public String getCode()
    {
        return code;
    }

    public void setCode(String code)
    {
        this.code = code;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getAddress()
    {
        return address;
    }

    public void setAddress(String address)
    {
        this.address = address;
    }
}
