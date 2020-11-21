// 将控制结构进行等价替换
#include <stdio.h>
#include <string.h>
using namespace std;
int n,sum,a[20];
bool b[100]={0},c[100]={0},d[100]={0};
char MAP[25][25];
void print()
{
    sum++;
    if(sum<=1)
    {

        int i = 1;
        while (i <= n){
            MAP[i][a[i]]='Q';
            i ++;
        }

    }
}
void queen(int i)
{
    for(int j=1;j<=n;j++)
    {
       if((b[j]==0)&&(c[i+j]==0)&&(d[i-j+n]==0))
       {
        a[i]=j;
        b[j]=1;
        c[i+j]=1;
        d[i-j+n]=1;
        if(i==n)
            print();
        else
            queen(i+1);
        b[j]=0;
        c[i+j]=0;
        d[i-j+n]=0;
        if(sum)
            break;
       }
    }
}
int main()
{
    while(~scanf("%d",&n)&&n){
        memset(MAP,'.',sizeof(MAP));
        memset(a,0,sizeof(a));
        memset(b,0,sizeof(b));
        memset(c,0,sizeof(c));
        memset(d,0,sizeof(d));
        sum=0;
        queen(1);
        if(sum){

            int i = 1, j = 1;
            while (i <= n){
                j = 1;
                while (j <= n){
                    printf("%c ",MAP[i][j]);
                    j ++;
                }
                printf("\n");
            }

        }
        else
            printf("No answer.\n");
        printf("\n");
    }
    return 0;
}
