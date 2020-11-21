// 改变数据类型
#include <stdio.h>
#include <string.h>
using namespace std;

long long n,sum,a[20];
int b[100]={0},c[100]={0},d[100]={0};
char MAP[25][25];


void print()
{
    sum++;
    if(sum<=1)
    {

        for(long long i=1;i<=n;i++){
            MAP[i][a[i]]='Q';
        }
    }
}
void queen(long long i)
{
    for(long long j=1;j<=n;j++)
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
            for(int i=1;i<=n;i++){
                for(int j=1;j<=n;j++)
                    printf("%c ",MAP[i][j]);
                printf("\n");
            }
        }
        else
            printf("No answer.\n");
        printf("\n");
    }
    return 0;
}
