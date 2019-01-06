package ad;

import com.google.common.collect.Lists;
import lombok.Data;
import lombok.ToString;

import java.util.Comparator;
import java.util.List;

/**
 * @author jzb 2018-11-15
 */
public class InitSilkCarRecord {


    public static void main(String[] args) {
        final Manager manager = new Manager();
        manager.add(new Item(5));
        manager.add(new Item(6));
        manager.add(new Item(4));
        manager.add(new Item(2));
        manager.add(new Item(1));
        manager.add(new Item(3));

        System.out.println(manager);
    }


    @Data
    public static class Manager {
        private List<Item> items = Lists.newArrayList();
        private Item latest;

        void add(Item item) {
            items.add(item);
            if (latest == null) {
                latest = item;
                return;
            }
//            if (item.count >= latest.count) {
//                latest.next = item;
//                item.prev = latest;
//                latest = item;
//                return;
//            }
            latest.push(item);
            if (item.count >= latest.count) {
                latest = item;
            }
            items.sort(Comparator.comparing(Item::getCount));
        }
    }

    @Data
    @ToString(onlyExplicitlyIncluded = true)
    public static class Item {
        @ToString.Include
        private int count;
        private Item prev;
        private Item next;

        public Item(int count) {
            this.count = count;
        }

        public List<Item> push(Item item) {
            if (prev != null && next != null) {
                if (item.count >= prev.count && item.count <= count) {
                    final List<Item> result = Lists.newArrayList(this, prev);
                    item.next = this;
                    item.prev = prev;
                    prev.next = item;
                    prev = item;
                    return result;
                }
            }

            if (prev == null && next == null) {
                if (item.count >= count) {
                    next = item;
                    item.prev = this;
                } else {
                    prev = item;
                    item.next = this;
                }
                return Lists.newArrayList(this);
            }

            if (item.count >= count) {
                if (next == null) {
                    next = item;
                    item.prev = this;
                    return Lists.newArrayList(this);
                } else {
                    return next.push(item);
                }
            } else {
                if (prev == null) {
                    prev = item;
                    item.next = this;
                    return Lists.newArrayList(this);
                } else {
                    return prev.push(item);
                }
            }
        }
    }

}
