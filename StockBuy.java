import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * @ClassName Stock
 * @Description TODO
 * @Author qping
 * @Date 2021/5/6 10:55
 * @Version 1.0
 **/
public class StockBuy {

    static class Stock {
        public int order;           // 展示排序
        public String stockName;    // 股票名
        public double lotPrice;     // 一手价格

        public double percent;      // 预期仓位百分比
        public double percentOld;      // 原始预期仓位百分比

        // 需要计算
        public int lotExpect;       // 预期（手数）
        public int lotMax;          // 最大（手数）
        public int lotFinal;        // 最终（手数）
        public double costFinal;    // 最终花费

        public Stock(String stockName, double unitPrice,double percent,double totalMoney, int order){
            this.stockName = stockName;
            this.lotPrice = unitPrice * 100;
            this.order = order;
            this.percentOld = percent;
            setPercent(totalMoney, percent);
        }
        public void setPercent(double totalMoney, double percent){
            this.percent = percent;
            this.lotExpect = (int) (totalMoney * (percent / 100) / lotPrice);
            this.lotMax = (int)(totalMoney / lotPrice);
        }
    }

    public static void main(String[] args) {

        double totalMoney = 50000;

        List<Stock> stocks = new ArrayList<>();
        stocks.add(new Stock("茅台", 1500, 20, totalMoney, 1));    // 茅台 20% 股价假设1500 ，预设20%仓位
        stocks.add(new Stock("美团", 20, 40, totalMoney, 2));     // 美团 40%
        stocks.add(new Stock("腾讯", 150, 40, totalMoney, 3));      // 腾讯 40%


        // 目标：尽可能投入市场，同时尽可能满足仓位配置比例

        double remainMoney = totalMoney;
        List<Stock> okStocks = new ArrayList<>();
        for(int i = 0; i < 10; i++){
            double remainMoneyThisTurn = remainMoney;
            remainMoney = buy(remainMoney, stocks, okStocks);

            if(remainMoneyThisTurn == remainMoney){
                break;
            }
        }

        okStocks.addAll(stocks);
        print(okStocks, totalMoney);
        System.out.println("剩余钱: " + remainMoney );

        /**
         * 运行结果
         * 股票名: 茅台 	 股票价格: 1500.0 	最终购买(手): 0 	 预期仓位：20.0% 	 实际仓位 0.0%
         * 股票名: 美团 	 股票价格: 20.0 	    最终购买(手): 17 	 预期仓位：40.0% 	 实际仓位 68.0%
         * 股票名: 腾讯 	 股票价格: 150.0 	    最终购买(手): 1 	 预期仓位：40.0% 	 实际仓位 30.0%
         * 剩余钱: 1000.0
         */
    }

    public static void print(List<Stock> stocks, double totalMoney){
        stocks.sort(new Comparator<Stock>() {
            @Override
            public int compare(Stock o1, Stock o2) {
                return o1.order == o2.order ? 0 : o1.order > o2.order ? 1 : -1;
            }
        });
        for(Stock stock : stocks){
            System.out.println(String.format("股票名: %s \t 股票价格: %s \t 最终购买(手): %s \t 预期仓位：%s%% \t 实际仓位 %s%%", stock.stockName, stock.lotPrice/100, stock.lotFinal, stock.percentOld, stock.costFinal / totalMoney * 100 ));
        }
    }

    public static double buy(double totalMoney, List<Stock> stocks, List<Stock> okStocks){
        // 去除掉一股也买不起的情况
        Iterator<Stock> stockItor = stocks.iterator();
        double percentTotal = 0d;
        while(stockItor.hasNext()){
            Stock stock = stockItor.next();
            // 去除花所有钱也买不起一手的股票
            if(stock.lotMax == 0){
                okStocks.add(stock);
                stockItor.remove();
            }else{
                percentTotal += stock.percent;
            }
        }
        // 重新分配权重
        for(Stock stock : stocks){
            stock.setPercent(totalMoney, stock.percent / percentTotal * 100 );
        }

        // 重新分配权重后，先购买预期仓位的股票
        double remainMoney = totalMoney;
        for(Stock stock : stocks){
            double expectCostMoney = stock.lotExpect * stock.lotPrice;
            // 没有买的股票
            if(expectCostMoney > remainMoney){
                continue;
            }else{
                remainMoney -= expectCostMoney;
                stock.lotFinal += stock.lotExpect;
                stock.costFinal += expectCostMoney;
            }
        }

        return remainMoney;
    }

}
